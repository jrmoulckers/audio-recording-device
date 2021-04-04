# Audio Recording Device

## Debug Application On Physical Watch
From https://developer.android.com/training/wearables/apps/debugging.

```
adb forward tcp:4444 localabstract:/adb-hub
adb connect 127.0.0.1:4444
```

If first command prints `error: more than one device/emulator`, run `adb kill-server` and retry prior step.

Press "OK" on the physical watch to allow debugging.