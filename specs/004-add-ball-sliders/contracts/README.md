# Balls Demo Control Contract Reference

Use the `/demos/balls/settings` endpoint to drive automated QA or integration harnesses for the Balls demo.

## Endpoints

| Method | Path                    | Description                                                           |
|--------|-------------------------|-----------------------------------------------------------------------|
| GET    | `/demos/balls/settings` | Returns the current structure size and cannonball mass configuration. |
| POST   | `/demos/balls/settings` | Applies a new configuration for the next frame (validation enforced). |

### Schema Summary

```json
{
  "structureSize": 8 | 12 | 16,
  "massMultiplier": 0.5 | 0.75 | 1.0 | 1.25 | 1.5 | 1.75 | 2.0
}
```

- `structureSize` controls the square body resolution (balls per side).
- `massMultiplier` scales projectile mass for upcoming shots.

Refer to `balls-demo-controls.yaml` for the complete OpenAPI definition.
