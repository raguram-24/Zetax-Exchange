/**
 * Engine threads: one per partition of the commands topic, using consumer.assign() (never subscribe()).
 * <p>TODO (phase 2): EngineRunner as a SmartLifecycle bean; poll, apply to book, produce events.
 */
package com.zetax.engine.runner;
