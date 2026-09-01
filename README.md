# Currency Converter — Trabajo Práctico #1

Desarrollo Profesional de Software (DPS) — ITBA, 2026, 2do cuatrimestre.

Extensión del proyecto `CurrencyConverter` visto en clase, integrando la API REST de
[freecurrencyapi.com](https://freecurrencyapi.com/).

## Grupo Alan-Kay

- Matías Romanato
- Julieta Techenski
- Valentina Marti Reta
- Tobías Noceda
- Matías Sapino

## Requisitos

- **JDK 25** (el build lo verifica con `maven-enforcer-plugin` y falla con cualquier otra versión)
- Maven 3.9+
- Una API key de freecurrencyapi.com

## Configuración

La API key se lee de la variable de entorno `FREECURRENCY_API_KEY`. No está en el código:
una credencial commiteada queda en el historial del repositorio para siempre.

```bash
# Linux / macOS / Git Bash
export FREECURRENCY_API_KEY=tu_api_key

# PowerShell
$env:FREECURRENCY_API_KEY="tu_api_key"
```

Desde IntelliJ: Run → Edit Configurations → campo **Environment variables**.

## Build y tests

```bash
mvn clean test
```

Los tests no salen a la red: el adapter se prueba contra un fake de `HttpClient` y
`UnirestHttpClient` contra un servidor WireMock local. Esto los hace rápidos,
determinísticos y permite forzar escenarios de error (404, 500, fallo de conexión) que
contra la API real serían imposibles de reproducir.

Para ver la cobertura desde IntelliJ: botón derecho sobre `src/test/java` →
More Run/Debug → Run 'All Tests' with Coverage.

## Uso

```bash
mvn compile exec:java "-Dexec.args=currencies"
mvn compile exec:java "-Dexec.args=rate USD EUR"
mvn compile exec:java "-Dexec.args=convert 100 USD EUR,JPY"
mvn compile exec:java "-Dexec.args=convert 100 USD EUR,JPY 2024-11-20"
```

| Comando                                       | Descripción                                                                        |
| --------------------------------------------- | ---------------------------------------------------------------------------------- |
| `currencies`                                  | Lista las monedas soportadas por la API                                            |
| `rate <origen> <destino>`                     | Cotización entre dos monedas                                                       |
| `convert <monto> <origen> <destinos> [fecha]` | Convierte a una o varias monedas, opcionalmente en una fecha pasada (`yyyy-MM-dd`) |

## Estructura

```text
ar.edu.itba.tp1.exchange
├── business/                    ← reglas de negocio, sin dependencias de infraestructura
│   ├── models/
│   │   ├── MoneyAmount                  monto + moneda, redondeo a 2 decimales
│   │   ├── ExchangeRate                 cotización entre dos monedas
│   │   └── HistoricalConversionResult   conversión, con la cotización usada y su timestamp
│   ├── CurrencyRateProvider     ← puerto: qué necesita el negocio
│   └── CurrencyConverter        ← casos de uso
├── providers/                   ← detalles
│   ├── http/
│   │   ├── HttpClient                   interfaz propia de cliente HTTP
│   │   ├── UnirestHttpClient            implementación con Unirest
│   │   ├── HttpApiResponse
│   │   └── exceptions/                  jerarquía de errores de la API
│   └── FreeCurrencyRateProvider ← adapter contra freecurrencyapi.com
└── Main                         ← arma el grafo de objetos, CLI
```

La dirección de las dependencias va siempre de `providers` hacia `business`, nunca al
revés: ninguna clase de `business` importa Unirest, Gson ni conoce códigos HTTP.

## Decisiones de diseño

- **`CurrencyRateProvider` como interfaz en el negocio.** El negocio declara qué necesita;
  `providers` decide cómo obtenerlo. Cambiar de proveedor de cotizaciones no toca `business`.
- **`HttpClient` propio en lugar de usar Unirest directamente.** Aísla al adapter de la
  librería HTTP: si se reemplaza Unirest por otra (OkHttp, el HttpClient del JDK), solo
  cambia `UnirestHttpClient`. Además permite testear el adapter sin salir a la red.
- **`ExchangeRate` en lugar de `Map<Currency, BigDecimal>`.** Un `Map` obliga a saber por
  convención qué significa la clave y deja la moneda base fuera del dato. Como record, la
  cotización se explica sola.
- **`BigDecimal` en todo el camino del dinero**, incluido el parseo del JSON. Pasar por
  `double` en cualquier punto pierde precisión aunque el resto del modelo use `BigDecimal`.
  Se redondea el monto a 2 decimales; la cotización mantiene su precisión completa.
- **El puerto expone el caso singular y el múltiple.** La API acepta varias monedas en un
  solo request, así que convertir a N monedas hace **una** llamada HTTP y no N. El caso de
  una sola moneda tiene su propio método en vez de envolverse en un `Set` de un elemento:
  así el error "falta la moneda que pedí" es explícito y no un `NoSuchElementException`.
- **`CurrencyConverter` solo convierte dinero.** Listar las monedas soportadas u obtener una
  cotización suelta no son conversiones: `Main` se las pide directamente al proveedor.
  Delegarlas desde el converter habría sido un pasamanos sin comportamiento propio.
- **`HistoricalConversionResult` se reutiliza para ambos casos.** La conversión con
  cotización actual y la histórica tienen la misma estructura; el `timestamp` se interpreta
  según el caso de uso: cuándo se obtuvo la cotización, o la fecha de vigencia consultada.
- **`Clock` inyectado en `CurrencyConverter`.** Hace determinísticos los tests de timestamp
  y de validación de fechas futuras.
- **Errores traducidos en el adapter.** El negocio nunca ve un código HTTP: los 4xx/5xx y
  los fallos de red se convierten en excepciones de la jerarquía `CurrencyApiException`.

## Funcionalidades

| # | Requerimiento                              | Dónde                                                 |
| - | ------------------------------------------ | ----------------------------------------------------- |
| 1 | Listar monedas soportadas                  | `CurrencyRateProvider.getSupportedCurrencies()`       |
| 2 | Timestamp de la cotización                 | `HistoricalConversionResult.timestamp()`              |
| 3 | Solo la cotización, sin monto              | `CurrencyRateProvider.getExchangeRate()`              |
| 4 | Manejo de errores de conexión y de la API  | `providers/http/exceptions/`, `UnirestHttpClient`     |
| 5 | Convertir a varias monedas a la vez        | `CurrencyConverter.convertMultiple()`                 |
| 6 | Cotización de una fecha pasada             | `CurrencyConverter.convertMultipleOnDate()`           |
| 7 | Ver la cotización usada en la respuesta    | `HistoricalConversionResult.exchangeRate()`           |
