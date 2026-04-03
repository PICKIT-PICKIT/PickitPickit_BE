package PickitPickit.store.domain;

/**
 * 재고 추적 방식
 * QUANTITY: stock_quantity 수량 기반
 * STATUS:   stock_status 상태 기반 (IN_STOCK / OUT_OF_STOCK)
 */
public enum InventoryMode {
    QUANTITY,
    STATUS
}
