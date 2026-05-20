- Added support for 26.1.x
- Fixed permissions for shift click
- Added distance formatting using `!!`, `!`, `$` and `*` at the start of the message, range is calculated from range in config
- Default range(x) is 100 

|  Type   | Range | Trigger  |
|:-------:|:-----:|:--------:|
| Whisper | 0.2x  |   `*`    |
| Mutter  | 0.33x |   `$`    |
|   Say   |  1x   | (Normal) |
|  Shout  | 1.5x  |   `!`    |
| Scream  |  2x   |   `!!`   |