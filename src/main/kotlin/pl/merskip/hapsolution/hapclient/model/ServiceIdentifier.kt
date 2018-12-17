package pl.merskip.hapsolution.hapclient.model

import java.util.*

enum class ServiceIdentifier(val uuid: UUID) {
    AccessoryInformation(UUID.fromString("0000003E-0000-1000-8000-0026BB765291")),
    AirQualitySensor(UUID.fromString("0000008D-0000-1000-8000-0026BB765291")),
    BatteryService(UUID.fromString("00000096-0000-1000-8000-0026BB765291")),
    BridgingState(UUID.fromString("00000062-0000-1000-8000-0026BB765291")),
    CarbonDioxideSensor(UUID.fromString("00000097-0000-1000-8000-0026BB765291")),
    CarbonMonoxideSensor(UUID.fromString("0000007F-0000-1000-8000-0026BB765291")),
    ContactSensor(UUID.fromString("00000080-0000-1000-8000-0026BB765291")),
    Door(UUID.fromString("00000081-0000-1000-8000-0026BB765291")),
    Fan(UUID.fromString("00000040-0000-1000-8000-0026BB765291")),
    GarageDoorOpener(UUID.fromString("00000041-0000-1000-8000-0026BB765291")),
    HumiditySensor(UUID.fromString("00000082-0000-1000-8000-0026BB765291")),
    LeakSensor(UUID.fromString("00000083-0000-1000-8000-0026BB765291")),
    LightSensor(UUID.fromString("00000084-0000-1000-8000-0026BB765291")),
    LightBulb(UUID.fromString("00000043-0000-1000-8000-0026BB765291")),
    LockManagement(UUID.fromString("00000044-0000-1000-8000-0026BB765291")),
    LockMechanism(UUID.fromString("00000045-0000-1000-8000-0026BB765291")),
    MotionSensor(UUID.fromString("00000085-0000-1000-8000-0026BB765291")),
    OccupancySensor(UUID.fromString("00000086-0000-1000-8000-0026BB765291")),
    Outlet(UUID.fromString("00000047-0000-1000-8000-0026BB765291")),
    SecuritySystem(UUID.fromString("0000007E-0000-1000-8000-0026BB765291")),
    SmokeSensor(UUID.fromString("00000087-0000-1000-8000-0026BB765291")),
    StatefulProgrammableSwitch(UUID.fromString("00000088-0000-1000-8000-0026BB765291")),
    StatelessProgrammableSwitch(UUID.fromString("00000089-0000-1000-8000-0026BB765291")),
    Switch(UUID.fromString("00000049-0000-1000-8000-0026BB765291")),
    TemperatureSensor(UUID.fromString("0000008A-0000-1000-8000-0026BB765291")),
    Thermostat(UUID.fromString("0000004A-0000-1000-8000-0026BB765291")),
    Window(UUID.fromString("0000008B-0000-1000-8000-0026BB765291")),
    WindowCovering(UUID.fromString("0000008C-0000-1000-8000-0026BB765291"))
}
