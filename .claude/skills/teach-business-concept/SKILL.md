---
name: teach-business-concept
description: Use when teaching a trading / exchange business concept to Raguram (order book, lifecycle, price-time priority, self-trade prevention, price band, halt, replay promise...) - at the start of each Java build step, when he asks "what does X mean", or in a business learning session. Uses only the vault design; updates GLOSSARY and PROGRESS_BUSINESS_LEARNING.
---

# Teach a business concept

Source of truth: the vault `~/Documents/Second_Brain/wiki/projects/order-matching-engine/`
(`Order Matching Engine - Overview.md` = decisions D-xx; `... Business and Technical Flow.md` = A1–A6, BF-xx, R-xx, B/TF sections).
Read the exact section first. **Never invent a rule.** If the vault doesn't decide it, say "not decided yet" and offer it as an open question.

## How to teach
1. **Check what's known:** `docs/GLOSSARY.md` and `docs/PROGRESS_BUSINESS_LEARNING.md` (including "Mistakes I made"). Don't re-teach; do revisit past mistakes when they're relevant.
2. **Everyday picture first** (the noticeboard, the shop price tag), then the real-exchange version, then the ZetaX rule with its ID (R-08, D-13…).
3. **A small book on screen** (ASCII, like the vault's A5): show before → command → after.
4. **Why real exchanges do it this way** (fairness, certainty, no loss: A2's three promises).
5. **Link to the code being built:** "this is the `if` in `OrderBook.match` that…", and which test proves it.
6. **Check questions (1–2), using a fresh book**, not the same example. Make him predict the outcome (which trades, at which price, what rests, which status, which events).
7. Wrong answer → explain the specific misunderstanding, record it, ask a new variant.

## After the concept clicks
- New terms → `docs/GLOSSARY.md` (term, meaning, example; mistakes in "Watch out").
- Session notes → `docs/PROGRESS_BUSINESS_LEARNING.md` (append-only entry in its format; tick the tracker row).
- Both are doc changes: show the entry and get approval (`java-approval-gate`) before writing.

## Style
Simple English, short sentences, numbers in ticks *and* display price where useful (101.00 → `10100`). Explain a trading term the first time it appears.
