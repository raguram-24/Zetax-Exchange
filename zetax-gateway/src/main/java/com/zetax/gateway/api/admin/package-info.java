/**
 * Admin API (D-20): symbols CRUD + halt / resume / delist, protected by X-Admin-Key.
 * Every change is saved to Postgres AND sent as a command to the commands topic.
 * <p>TODO (phase 3): see BF-08, TF-08.
 */
package com.zetax.gateway.api.admin;
