package quotes

import (
	"fmt"
	"strconv"
	"strings"
	"time"
)

// ParseLine converts a raw driver line into a normalized quote.
func ParseLine(line, source string) (Quote, error) {
	fields := strings.Fields(line)
	if len(fields) != 3 {
		return Quote{}, fmt.Errorf("expected 3 fields, got %d", len(fields))
	}

	price, err := strconv.ParseFloat(fields[1], 64)
	if err != nil {
		return Quote{}, fmt.Errorf("invalid price: %w", err)
	}
	if price <= 0 {
		return Quote{}, fmt.Errorf("price must be positive")
	}

	timestamp, err := time.Parse(time.RFC3339, fields[2])
	if err != nil {
		return Quote{}, fmt.Errorf("invalid timestamp: %w", err)
	}

	return Quote{
		Ticker:    strings.ToUpper(fields[0]),
		Price:     price,
		Timestamp: timestamp.UTC().Format(time.RFC3339),
		Source:    source,
	}, nil
}
