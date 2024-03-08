package at.itexperts.orderprocessing;

public record OutgoingOrder(long id, long productId, ProductCategory category) {}
