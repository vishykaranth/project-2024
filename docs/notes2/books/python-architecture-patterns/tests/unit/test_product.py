from datetime import date

from src.domain import events
from src.domain.model import (
    Batch,
    OrderLine,
    Product,
)


def test_prefers_current_stock_batches_to_shipments():
    in_stock_batch = Batch(reference="in-stock-batch", sku="RETRO-CLOCK", purchased_quantity=100, eta=None)
    shipment_batch = Batch(reference="shipment-batch", sku="RETRO-CLOCK", purchased_quantity=100, eta=None)
    line = OrderLine(order_id="oref", sku="RETRO-CLOCK", qty=10)
    product = Product(sku="RETRO-CLOCK", batches=[in_stock_batch, shipment_batch])

    product.allocate(line)

    assert in_stock_batch.available_quantity == 90
    assert shipment_batch.available_quantity == 100


def test_prefers_earlier_batches():
    earliest = Batch(reference="speedy-batch", sku="MINIMALIST-SPOON", purchased_quantity=100, eta=date(2022, 1, 7))
    medium = Batch(reference="normal-batch", sku="MINIMALIST-SPOON", purchased_quantity=100, eta=date(2022, 1, 8))
    latest = Batch(reference="slow-batch", sku="MINIMALIST-SPOON", purchased_quantity=100, eta=date(2022, 1, 9))
    line = OrderLine(order_id="oref", sku="MINIMALIST-SPOON", qty=10)
    product = Product(sku="MINIMALIST-SPOON", batches=[medium, earliest, latest])

    product.allocate(line)

    assert earliest.available_quantity == 90
    assert medium.available_quantity == 100
    assert latest.available_quantity == 100


def test_returns_allocated_batch_ref():
    in_stock_batch = Batch(reference="in-stock-batch-ref", sku="HIGHBROW-POSTER", purchased_quantity=100, eta=None)
    shipment_batch = Batch(reference="shipment-batch-ref", sku="HIGHBROW-POSTER", purchased_quantity=100, eta=date(2022, 1, 7))
    line = OrderLine(order_id="oref", sku="HIGHBROW-POSTER", qty=10)
    product = Product(sku="HIGHBROW-POSTER", batches=[in_stock_batch, shipment_batch])

    allocation = product.allocate(line)

    assert allocation == in_stock_batch.reference


def test_records_out_of_stock_event_if_cannot_allocate():
    batch = Batch(reference="batch", sku="SMALL-FORM", purchased_quantity=10, eta=date(2022, 1, 7))
    product = Product(sku="SMALL-FORK", batches=[batch])
    product.allocate(OrderLine(order_id="oref", sku="SMALL-FORM", qty=10))

    allocation = product.allocate(OrderLine(order_id="oref", sku="SMALL-FORM", qty=1))

    assert product.messages[-1] == events.OutOfStock(sku="SMALL-FORM")
    assert allocation is None
