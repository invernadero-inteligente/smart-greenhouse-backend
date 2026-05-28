# Módulo IOT-IA - Guia de uso

## 1. Servicios
### 1.1. Cambiar estado de los actuadores

```shell
PATH: /api/iot/actuator/event/{zoneId}
METHOD: POST
Headers:
  Content-Type: application/json
Body:
{
  "name" : "FAN",
  "action" : "ON"
}
```
Este servicio se encargará de encender y apagar de forma remota los actuadores de cada zona. El servicio recibirá el id de la zona, el nombre del actuador y la acción a ejecutar (ON/OFF). El servicio se encargará de enviar el comando al microcontrolador correspondiente para que ejecute la acción.

### 1.2. Tomar foto de la zona

```shell
PATH: /api/iot/camera/phot/request/{zoneId}
METHOD: POST
Headers:
  Content-Type: application/json
Body:
```
Este servicio se encargará de tomar una foto de la zona solicitada. El servicio recibirá el id de la zona y se encargará de enviar el comando al microcontrolador correspondiente para que tome la foto. La imagen será analizada por un modelo LLM para detectar si hay personas en la zona. El resultado del análisis se guardará en la base de datos y se podrá consultar posteriormente.
El body que retorna el LLM se concatenó en el campo de descripción de la tabla ai_results, con el formato:
```
cultivo=tomate; anomalia.cantidad=1; anomalia.detectada=true; conteo.maduro=0; conteo.pinton=0; conteo.verde=0; total=0
```
