package awsm.banking;

import static awsm.banking.Transactions.Transaction.Type.DEPOSIT;
import static awsm.banking.Transactions.Transaction.Type.WITHDRAWAL;
import static awsm.infrastructure.time.TimeMachine.clock;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static jooq.tables.BankAccountTx.BANK_ACCOUNT_TX;

import awsm.banking.Transactions.Transaction.Type;
import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import jooq.tables.records.BankAccountTxRecord;
import one.util.streamex.StreamEx;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;
import org.threeten.extra.LocalDateRange;

class Transactions {

  private final BankAccount bankAccount;

  private final ImmutableList<Transaction> transactions;

  Transactions(BankAccount bankAccount) {
    this(bankAccount, emptyList());
  }

  private Transactions(BankAccount bankAccount, List<Transaction> transactions) {
    this.bankAccount = bankAccount;
    this.transactions = ImmutableList.copyOf(transactions);
  }

  Transactions thatAre(Predicate<Transaction> condition) {
    return new Transactions(
        bankAccount,
        transactions
            .stream()
            .filter(condition)
            .collect(toList())
    );
  }

  Amount balance() {
    return balance(Amount.amount("0.00"), (balance, tx) -> {});
  }

  Amount balance(Amount seed, Interims interims) {
    return StreamEx.of(transactions).foldLeft(seed, (amount, tx) -> {
      var balance = tx.apply(amount);
      interims.interim(tx, balance);
      return balance;
    });
  }

  interface Interims {
    void interim(Transaction tx, Amount balance);
  }

  Transactions with(Transaction tx) {
    return new Transactions(
        bankAccount,
        ImmutableList.<Transaction>builder()
          .addAll(transactions)
          .add(tx)
          .build());
  }

  void saveNew(Repository repository) {
    repository.insert(this);
  }

  void delete(Repository repository) {
    repository.delete(this);
  }

  static class Transaction {

    public enum Type {
      DEPOSIT, WITHDRAWAL
    }

    private final Amount amount;

    private final LocalDateTime bookingTime;

    private final LocalDate bookingDate;

    private final Type type;

    private Transaction(Type type, Amount amount, LocalDateTime bookingTime) {
      this.type = type;
      this.amount = amount;
      this.bookingTime = bookingTime;
      this.bookingDate = bookingTime.toLocalDate();
    }

    LocalDateTime bookingTime() {
      return bookingTime;
    }

    Amount apply(Amount balance) {
      if (type == DEPOSIT) {
        return balance.add(amount);
      }
      if (type == WITHDRAWAL) {
        return balance.subtract(amount);
      }
      throw new IllegalStateException("Illegal type " + type);
    }

    Amount withdrawn() {
      return isWithdrawal() ? amount : Amount.amount("0.00");
    }

    boolean isWithdrawal() {
      return type == WITHDRAWAL;
    }

    Amount deposited() {
      return isDeposit() ? amount : Amount.amount("0.00");
    }

    boolean isDeposit() {
      return type == DEPOSIT;
    }

    boolean bookedIn(LocalDate date) {
      return bookingDate.isEqual(date);
    }

    boolean bookedIn(Month month) {
      return bookingDate.getMonth().equals(month);
    }

    boolean bookedBefore(LocalDate date) {
      return LocalDateRange.ofUnboundedStart(date).contains(bookingDate);
    }

    boolean bookedDuring(LocalDate from, LocalDate to) {
      return LocalDateRange.ofClosed(from, to).contains(bookingDate);
    }

    static Transaction withdrawalOf(Money money) {
      return new Transaction(WITHDRAWAL, money.amount(), LocalDateTime.now(clock()));
    }

    static Transaction depositOf(Money money) {
      return new Transaction(DEPOSIT, money.amount(), LocalDateTime.now(clock()));
    }

  }

  @Component
  static class Repository {

    private final DSLContext dsl;

    Repository(DSLContext dsl) {
      this.dsl = dsl;
    }

    private void insert(Transactions self) {
      self
          .transactions
          .forEach(tx -> dsl
              .insertInto(BANK_ACCOUNT_TX)
              .set(BANK_ACCOUNT_TX.BANK_ACCOUNT_ID, self.bankAccount.id().asLong())
              .set(BANK_ACCOUNT_TX.AMOUNT, tx.amount.decimal())
              .set(BANK_ACCOUNT_TX.BOOKING_TIME, tx.bookingTime)
              .set(BANK_ACCOUNT_TX.TYPE, tx.type.name())
              .execute());
    }

    private void delete(Transactions self) {
      dsl
          .deleteFrom(BANK_ACCOUNT_TX)
          .where(BANK_ACCOUNT_TX.BANK_ACCOUNT_ID.eq(self.bankAccount.id().asLong()))
          .execute();
    }

    Transactions listBy(BankAccount bankAccount) {
      return new Transactions(
          bankAccount,
          dsl
          .selectFrom(BANK_ACCOUNT_TX)
          .where(BANK_ACCOUNT_TX.BANK_ACCOUNT_ID.equal(bankAccount.id().asLong()))
          .orderBy(BANK_ACCOUNT_TX.INDEX.asc())
          .fetchStream()
          .map(fromJooq())
          .collect(toList()));
    }

    private Function<BankAccountTxRecord, Transaction> fromJooq() {
      return jooq -> new Transaction(
          Type.valueOf(jooq.getType()),
          Amount.amount(jooq.getAmount()),
          jooq.getBookingTime()
      );
    }

  }
}