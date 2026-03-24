package quotes

import "testing"

func TestParseLine(t *testing.T) {
	t.Parallel()

	item, err := ParseLine("GAZP 172.35 2026-03-24T15:10:00Z", "linux-driver")
	if err != nil {
		t.Fatalf("ParseLine returned error: %v", err)
	}

	if item.Ticker != "GAZP" {
		t.Fatalf("unexpected ticker: %s", item.Ticker)
	}
	if item.Price != 172.35 {
		t.Fatalf("unexpected price: %f", item.Price)
	}
	if item.Timestamp != "2026-03-24T15:10:00Z" {
		t.Fatalf("unexpected timestamp: %s", item.Timestamp)
	}
	if item.Source != "linux-driver" {
		t.Fatalf("unexpected source: %s", item.Source)
	}
}

func TestParseLineRejectsInvalidPrice(t *testing.T) {
	t.Parallel()

	if _, err := ParseLine("GAZP nope 2026-03-24T15:10:00Z", "linux-driver"); err == nil {
		t.Fatal("expected ParseLine to reject invalid price")
	}
}
