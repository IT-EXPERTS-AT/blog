package at.itexperts.datasourceproxy;

public record ParsedQuery(String rawQuery, net.sf.jsqlparser.statement.Statement statement) {}
