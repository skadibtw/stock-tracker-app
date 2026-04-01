package app

import (
	"context"
	"encoding/json"
	"sync"

	"github.com/redis/go-redis/v9"
	"stock-tracker-app/quotes-service/internal/quotes"
)

type RedisPublisher struct {
	client    *redis.Client
	stream    string
	keyPrefix string

	mu        sync.RWMutex
	healthy   bool
	lastError string
}

func NewRedisPublisher(ctx context.Context, cfg Config) (*RedisPublisher, error) {
	options, err := redis.ParseURL(cfg.RedisURL)
	if err != nil {
		return nil, err
	}

	client := redis.NewClient(options)
	if err := client.Ping(ctx).Err(); err != nil {
		return nil, err
	}

	return &RedisPublisher{
		client:    client,
		stream:    cfg.RedisStream,
		keyPrefix: cfg.RedisKeyPrefix,
		healthy:   true,
	}, nil
}

func (p *RedisPublisher) Store(ctx context.Context, items []quotes.Quote) error {
	pipe := p.client.Pipeline()
	for _, item := range items {
		payload, err := json.Marshal(item)
		if err != nil {
			p.markFailure(err.Error())
			return err
		}

		pipe.Set(ctx, p.keyPrefix+item.Ticker, payload, 0)
		pipe.XAdd(ctx, &redis.XAddArgs{
			Stream: p.stream,
			MaxLen: 100000,
			Approx: true,
			Values: map[string]any{
				"ticker":    item.Ticker,
				"price":     item.Price,
				"timestamp": item.Timestamp,
				"source":    item.Source,
			},
		})
	}

	_, err := pipe.Exec(ctx)
	if err != nil {
		p.markFailure(err.Error())
		return err
	}

	p.markHealthy()
	return nil
}

func (p *RedisPublisher) DependencyStatus() quotes.DependencyStatus {
	p.mu.RLock()
	defer p.mu.RUnlock()

	status := "up"
	if !p.healthy {
		status = "down"
	}

	return quotes.DependencyStatus{
		Name:      "redis",
		Status:    status,
		LastError: p.lastError,
	}
}

func (p *RedisPublisher) Close() error {
	return p.client.Close()
}

func (p *RedisPublisher) markHealthy() {
	p.mu.Lock()
	defer p.mu.Unlock()
	p.healthy = true
	p.lastError = ""
}

func (p *RedisPublisher) markFailure(message string) {
	p.mu.Lock()
	defer p.mu.Unlock()
	p.healthy = false
	p.lastError = message
}
