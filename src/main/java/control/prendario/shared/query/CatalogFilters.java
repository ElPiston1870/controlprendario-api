package control.prendario.shared.query;

import static jakarta.persistence.criteria.JoinType.LEFT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import control.prendario.audit.domain.AuditEvent;
import control.prendario.client.domain.Client;
import control.prendario.loan.domain.Loan;
import control.prendario.loan.domain.LoanStatus;
import control.prendario.loan.domain.MachineryLoan;
import control.prendario.movement.domain.Movement;
import control.prendario.payment.domain.MachineryPayment;
import control.prendario.payment.domain.Payment;
import control.prendario.vehicle.domain.Vehicle;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.From;
import org.springframework.data.jpa.domain.Specification;

public final class CatalogFilters {
    private CatalogFilters() {}

    public static Specification<Client> clients(BrowseQuery q) {
        return (client, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            conditions.add(active(cb, client));
            clientFields(q, cb, client, conditions);
            conditions.addAll(words(q.termino(), token -> personToken(cb, client, token, true)));
            return cb.and(conditions.toArray(Predicate[]::new));
        };
    }

    public static Specification<Vehicle> vehicles(BrowseQuery q) {
        return (vehicle, query, cb) -> cb.and(active(cb, vehicle),
                cb.and(words(q.termino(), token -> cb.or(
                        contains(cb, vehicle.get("plate"), token),
                        contains(cb, vehicle.get("brand"), token),
                        contains(cb, vehicle.get("owner"), token),
                        contains(cb, vehicle.get("line"), token)
                )).toArray(Predicate[]::new)));
    }

    public static Specification<Loan> loans(BrowseQuery q, boolean overdue) {
        return loans(q, overdue, "loanId", true, root -> {
            var vehicle = root.join("vehicle", LEFT);
            return List.of(vehicle.get("plate"), vehicle.get("brand"), vehicle.get("line"));
        });
    }

    public static Specification<MachineryLoan> machineryLoans(BrowseQuery q, boolean overdue) {
        return loans(q, overdue, "machineryLoanId", false,
                root -> List.of(root.get("machineType"), root.get("machineBrand")));
    }

    private static <T> Specification<T> loans(BrowseQuery q, boolean overdue, String idField,
                                               boolean vehicleLoan,
                                               Function<Root<T>, List<Expression<String>>> collateral) {
        return (root, query, cb) -> {
            if (contentQuery(query)) {
                root.fetch("client", LEFT);
                if (vehicleLoan) root.fetch("vehicle", LEFT);
            }
            List<Predicate> conditions = new ArrayList<>();
            conditions.add(active(cb, root));
            var client = usesClient(q) ? root.join("client", LEFT) : null;
            if (client != null) clientFields(q, cb, client, conditions);
            if (q.id() != null) conditions.add(cb.equal(root.get(idField), q.id()));
            if (overdue) conditions.add(cb.equal(root.get("loanStatus"), LoanStatus.VENCIDO));
            if (hasText(q.estado())) conditions.add(cb.equal(root.get("loanStatus"), value(LoanStatus.class, q.estado())));
            day(cb, root.get("loanDate"), q.fecha(), conditions);
            if (q.vencimiento() != null) {
                if (vehicleLoan) day(cb, root.get("dueDate"), q.vencimiento(), conditions);
                else conditions.add(cb.equal(root.get("dueDate"), q.vencimiento()));
            }
            List<Expression<String>> descriptions = hasText(q.termino()) ? collateral.apply(root) : List.of();
            conditions.addAll(words(q.termino(), token -> {
                List<Predicate> choices = new ArrayList<>();
                choices.add(personToken(cb, client, token, true));
                descriptions.forEach(field -> choices.add(contains(cb, field, token)));
                try { choices.add(cb.equal(root.get(idField), Long.parseLong(token))); }
                catch (NumberFormatException ignored) { /* Names and plates need no numeric comparison. */ }
                return cb.or(choices.toArray(Predicate[]::new));
            }));
            return cb.and(conditions.toArray(Predicate[]::new));
        };
    }

    public static Specification<Payment> payments(BrowseQuery q) {
        return payments(q, "loan");
    }

    public static Specification<MachineryPayment> machineryPayments(BrowseQuery q) {
        return payments(q, "machineryLoan");
    }

    private static <T> Specification<T> payments(BrowseQuery q, String loanRelation) {
        return (payment, query, cb) -> {
            if (contentQuery(query)) payment.fetch(loanRelation, LEFT).fetch("client", LEFT);
            List<Predicate> conditions = new ArrayList<>();
            if (usesClient(q)) {
                var client = payment.join(loanRelation, LEFT).join("client", LEFT);
                clientFields(q, cb, client, conditions);
                conditions.addAll(words(q.termino(), token -> personToken(cb, client, token, true)));
            }
            day(cb, payment.get("paymentDate"), q.fecha(), conditions);
            if (hasText(q.tipoPago())) conditions.add(cb.equal(payment.get("paymentType"),
                    value(Payment.PaymentType.class, q.tipoPago())));
            if (hasText(q.metodoPago())) conditions.add(cb.equal(payment.get("paymentMethod"),
                    value(Payment.PaymentMethod.class, q.metodoPago())));
            return cb.and(conditions.toArray(Predicate[]::new));
        };
    }

    public static Specification<Movement> movements(BrowseQuery q) {
        return (movement, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            day(cb, movement.get("movementDate"), q.fecha(), conditions);
            if (hasText(q.termino())) conditions.add(cb.equal(movement.get("movementType"),
                    value(Movement.MovementType.class, q.termino())));
            return cb.and(conditions.toArray(Predicate[]::new));
        };
    }

    public static Specification<AuditEvent> events(BrowseQuery q) {
        return (event, query, cb) -> {
            List<Predicate> conditions = words(q.termino(), token -> cb.or(
                    contains(cb, event.get("username"), token), contains(cb, event.get("action"), token),
                    contains(cb, event.get("resource"), token), contains(cb, event.get("result"), token)));
            day(cb, event.get("eventDate"), q.fecha(), conditions);
            return cb.and(conditions.toArray(Predicate[]::new));
        };
    }

    private static boolean contentQuery(CriteriaQuery<?> query) {
        return query != null && query.getResultType() != Long.class && query.getResultType() != long.class;
    }

    private static boolean usesClient(BrowseQuery q) {
        return hasText(q.termino()) || hasText(q.clientName()) || hasText(q.numeroDocumento());
    }

    private static void clientFields(BrowseQuery q, CriteriaBuilder cb, From<?, ?> client, List<Predicate> conditions) {
        conditions.addAll(words(q.clientName(), token -> personToken(cb, client, token, false)));
        if (hasText(q.numeroDocumento())) {
            conditions.add(contains(cb, client.get("documentNumber"), q.numeroDocumento()));
        }
    }

    private static Predicate personToken(CriteriaBuilder cb, Path<?> person, String token, boolean document) {
        Predicate first = contains(cb, person.get("firstNames"), token);
        Predicate last = contains(cb, person.get("lastNames"), token);
        return document ? cb.or(first, last, contains(cb, person.get("documentNumber"), token)) : cb.or(first, last);
    }

    private static Predicate active(CriteriaBuilder cb, Path<?> root) {
        return cb.or(cb.isTrue(root.get("active")), cb.isNull(root.get("active")));
    }

    private static Predicate contains(CriteriaBuilder cb, Expression<String> field, String token) {
        return cb.like(cb.lower(field), pattern(token), '!');
    }

    public static String pattern(String token) {
        return "%" + token.strip().toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }

    public static List<String> tokens(String value) {
        return hasText(value)
                ? java.util.Arrays.stream(value.trim().split("\\s+")).limit(10).toList()
                : List.of();
    }

    private static List<Predicate> words(String value, Function<String, Predicate> condition) {
        return new ArrayList<>(tokens(value).stream().map(condition).toList());
    }

    public static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static void day(CriteriaBuilder cb, Path<LocalDateTime> field, LocalDate date, List<Predicate> clauses) {
        if (date != null) {
            clauses.add(cb.greaterThanOrEqualTo(field, date.atStartOfDay()));
            clauses.add(cb.lessThan(field, date.plusDays(1).atStartOfDay()));
        }
    }

    private static <E extends Enum<E>> E value(Class<E> type, String token) {
        return Enum.valueOf(type, token.trim().toUpperCase(Locale.ROOT));
    }
}
