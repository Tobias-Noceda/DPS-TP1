# Currency Converter — TP1

**Desarrollo de Software Profesional — 2026 — ITBA**

Conversor de monedas por consola que consume la API de
[freecurrencyapi.com](https://freecurrencyapi.com/). Permite listar las monedas soportadas,
consultar cotizaciones (actuales e históricas) y convertir un monto a una o varias monedas,
mostrando siempre la cotización usada y la marca de tiempo del dato.

---

## Cómo correr

**Requisitos:** JDK 25 y Maven.

```bash
# monedas soportadas
mvn compile exec:java "-Dexec.args=currencies"

# cotizacion entre dos monedas
mvn compile exec:java "-Dexec.args=rate USD EUR"

# convertir un monto a una o varias monedas
mvn compile exec:java "-Dexec.args=convert 100 USD EUR,JPY"

# lo mismo, para una fecha pasada
mvn compile exec:java "-Dexec.args=convert 100 USD EUR,JPY 2024-11-20"
```

Sin argumentos (o con un comando desconocido) imprime la ayuda. Salida de ejemplo:

```
$ mvn compile exec:java "-Dexec.args=convert 100 USD EUR,JPY"
100 USD -> 85.61 EUR (cotizacion: 0.8560581139, obtenida el 2026-08-23T21:20:31)
100 USD -> 15891.20 JPY (cotizacion: 158.9120211446, obtenida el 2026-08-23T21:20:31)

$ mvn compile exec:java "-Dexec.args=convert 100 USD EUR,JPY 2024-11-20"
100 USD -> 94.81 EUR (cotizacion: 0.9480900974, obtenida el 2024-11-20T00:00)
100 USD -> 15527.21 JPY (cotizacion: 155.2721421669, obtenida el 2024-11-20T00:00)
```

Los errores (moneda inexistente, monto o fecha mal escritos, fallas de la API o de
conexión) se informan con un mensaje claro:

```
$ mvn compile exec:java "-Dexec.args=rate USD XYZ"
'XYZ' no es un codigo de moneda ISO valido.

$ mvn compile exec:java "-Dexec.args=convert 100 USD EUR 2999-01-01"
La API respondio con error 422: {"message":"Validation error", ...}
```

## Cómo correr los tests

```bash
mvn test
```

---

## Estructura del proyecto

```
src/main/java/ar/edu/itba/tp1/exchange/
├── Main.java                          Unica clase de presentación: parsea los argumentos, arma
│                                      las dependencias, invoca al negocio e imprime el resultado
│
├── bussiness/                         REGLAS DE NEGOCIO (no conocen HTTP, JSON ni consola)
│   ├── CurrencyConverter.java             Convierte montos: a una moneda, a varias, y a varias en una fecha
│   ├── CurrencyRateLookup.java            Consulta monedas soportadas y cotizaciones (sin convertir montos)
│   ├── CurrencyRateProvider.java          Puerto: lo que el negocio necesita de un proveedor de cotizaciones
│   └── models/
│       ├── MoneyAmount.java               Record inmutable: monto + moneda
│       └── ConvertedMoneyAmount.java      Record inmutable: monto original, convertido, cotización y timestamp
│
└── providers/                         DETALLE: implementación contra freecurrencyapi.com
    ├── FreeCurrencyRateProvider.java      Implementa CurrencyRateProvider: arma los requests y parsea el JSON
    └── http/
        ├── HttpClient.java                Abstracción del cliente HTTP (permite testear sin red)
        ├── UnirestHttpClient.java         Implementación con Unirest + traducción de status codes a excepciones
        ├── HttpApiResponse.java           Record de la respuesta: status code + body
        └── exceptions/                    CurrencyApiException y sus especializaciones (conexión, 4xx, 5xx)

src/test/java/ar/edu/itba/
├── exchange/CurrencyConverterTest.java                    Negocio: conversiones y lookups (con provider stub)
└── tp1/exchange/providers/http/
    ├── UnirestHttpClientTest.java                         Cliente HTTP contra WireMock (200, 404, 500, sin red)
    └── HttpApiResponseTest.java                           Clasificación de status codes
```

---

## Funcionalidades → dónde están implementadas

| # | Funcionalidad | Implementación | Test |
|---|---|---|---|
| 1 | Listar todas las monedas soportadas | `CurrencyRateLookup.getSupportedCurrencies` → `FreeCurrencyRateProvider.getSupportedCurrencies` | `CurrencyConverterTest.testGetSupportedCurrencies` |
| 2 | Timestamp de la cotización en la respuesta | Campo `timestamp` de `ConvertedMoneyAmount`, seteado en `CurrencyConverter` | `CurrencyConverterTest.testConvertMultipleOnDate` |
| 3 | Obtener solo la cotización entre dos monedas | `CurrencyRateLookup.getExchangeRate` | `CurrencyConverterTest.testGetExchangeRate` |
| 4 | Manejo y notificación clara de errores | `UnirestHttpClient` traduce status codes y fallas de red a `CurrencyApiException`; `Main` las atrapa y muestra el mensaje | `UnirestHttpClientTest` |
| 5 | Convertir un monto a varias monedas a la vez | `CurrencyConverter.convertMultiple` | `CurrencyConverterTest.testConvertMultiple` |
| 6 | Cotización de una fecha pasada | `CurrencyConverter.convertMultipleOnDate` → endpoint `/historical` | `CurrencyConverterTest.testConvertMultipleOnDate` |
| 7 | Ver la cotización usada para cada moneda | Campo `rate` de `ConvertedMoneyAmount`, impreso por `Main.printConversion` | `CurrencyConverterTest.testConvert` |

---

## Decisiones de diseño (resumen)

- **El negocio no sabe que existe una API.** `CurrencyConverter` y `CurrencyRateLookup`
  dependen de la interfaz `CurrencyRateProvider`, que vive en `bussiness`: es el negocio el
  que define qué necesita, y `providers` el que se adapta.
- **`HttpClient` es una segunda abstracción**, para poder probar el parseo del JSON y el
  manejo de status codes sin salir a la red.
- **Excepciones propias** (`CurrencyApiException` y derivadas): ni el negocio ni `Main`
  conocen a Unirest.
- **Modelos inmutables** (`records`) y `BigDecimal` para el dinero, nunca `double`.
- **`Main` es la única clase de presentación**: parsea argumentos, arma las dependencias e
  imprime. No tiene reglas de negocio.
