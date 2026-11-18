import type { ReactNode } from 'react'

type PanelProps = {
  title: string
  area: string
  children?: ReactNode
}

/** A titled box placed in one cell of the trading-screen grid. */
export function Panel({ title, area, children }: PanelProps) {
  return (
    <section className="panel" style={{ gridArea: area }}>
      <h2 className="panel-title">{title}</h2>
      <div className="panel-body">{children ?? <p className="placeholder">Coming soon</p>}</div>
    </section>
  )
}
