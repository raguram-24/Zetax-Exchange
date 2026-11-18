// Mirrors zetax-common enums. Prices and quantities are integers (ticks / lots), never floats (D-13).

export type Side = 'BUY' | 'SELL'
export type OrderType = 'LIMIT' | 'MARKET'
export type OrderStatus =
  | 'RECEIVED'
  | 'OPEN'
  | 'PARTIALLY_FILLED'
  | 'FILLED'
  | 'CANCELLED'
  | 'REJECTED'
export type SymbolStatus = 'ACTIVE' | 'HALTED' | 'DELISTED'

// TODO (phase 3b): Order, Trade, BookLevel, BookSnapshot, BookDelta types matching the gateway's JSON.
