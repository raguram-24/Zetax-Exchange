import './App.css'
import { MyOrders } from './components/MyOrders'
import { OrderBook } from './components/OrderBook'
import { OrderForm } from './components/OrderForm'
import { PriceChart } from './components/PriceChart'
import { RecentTrades } from './components/RecentTrades'
import { SymbolList } from './components/SymbolList'
import { USER_ID } from './config/env'

export default function App() {
  return (
    <div className="app">
      <header className="topbar">
        <span className="brand">ZetaX</span>
        <span className="user">Trader: {USER_ID}</span>
      </header>
      <main className="grid">
        <SymbolList />
        <PriceChart />
        <OrderForm />
        <OrderBook />
        <RecentTrades />
        <MyOrders />
      </main>
    </div>
  )
}
