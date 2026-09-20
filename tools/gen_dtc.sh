#!/bin/sh
# Generates the bundled OBD-II trouble code dictionary.
#
# Output: app/src/main/assets/dtc/{en,ja,zh}.tsv  ("CODE<TAB>description")
#
# Only the systematic SAE J2012 generic families are emitted here: their text
# follows a defined pattern per bank / sensor / cylinder, so it can be produced
# from one template per family and stays accurate. Codes outside these families
# - manufacturer-specific P1xxx above all - are deliberately absent and are
# described at runtime by DtcDecoder from the structure of the code itself.
#
# Run from anywhere:  sh tools/gen_dtc.sh
set -e

ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT="$ROOT/app/src/main/assets/dtc"
mkdir -p "$OUT"

EN="$OUT/en.tsv"
JA="$OUT/ja.tsv"
ZH="$OUT/zh.tsv"
: > "$EN"
: > "$JA"
: > "$ZH"

# emit <code> <en> <ja> <zh>
emit() {
    printf '%s\t%s\n' "$1" "$2" >> "$EN"
    printf '%s\t%s\n' "$1" "$3" >> "$JA"
    printf '%s\t%s\n' "$1" "$4" >> "$ZH"
}

# code <letter> <number>  ->  e.g. P0301
code() {
    printf '%s%04d' "$1" "$2"
}

# ---------------------------------------------------------------- fuel & air
emit P0100 "Mass or Volume Air Flow Circuit" "エアフローセンサー回路" "空气流量传感器电路"
emit P0101 "Mass or Volume Air Flow Circuit Range/Performance" "エアフローセンサー回路 範囲/性能" "空气流量传感器电路 范围/性能"
emit P0102 "Mass or Volume Air Flow Circuit Low Input" "エアフローセンサー回路 入力低" "空气流量传感器电路 输入过低"
emit P0103 "Mass or Volume Air Flow Circuit High Input" "エアフローセンサー回路 入力高" "空气流量传感器电路 输入过高"
emit P0104 "Mass or Volume Air Flow Circuit Intermittent" "エアフローセンサー回路 断続" "空气流量传感器电路 间歇故障"
emit P0105 "Manifold Absolute Pressure/Barometric Pressure Circuit" "吸気圧センサー回路" "进气歧管绝对压力传感器电路"
emit P0106 "Manifold Absolute Pressure/Barometric Pressure Range/Performance" "吸気圧センサー回路 範囲/性能" "进气歧管绝对压力 范围/性能"
emit P0107 "Manifold Absolute Pressure/Barometric Pressure Low Input" "吸気圧センサー回路 入力低" "进气歧管绝对压力 输入过低"
emit P0108 "Manifold Absolute Pressure/Barometric Pressure High Input" "吸気圧センサー回路 入力高" "进气歧管绝对压力 输入过高"
emit P0109 "Manifold Absolute Pressure/Barometric Pressure Intermittent" "吸気圧センサー回路 断続" "进气歧管绝对压力 间歇故障"
emit P0110 "Intake Air Temperature Sensor Circuit" "吸気温センサー回路" "进气温度传感器电路"
emit P0111 "Intake Air Temperature Sensor Circuit Range/Performance" "吸気温センサー回路 範囲/性能" "进气温度传感器 范围/性能"
emit P0112 "Intake Air Temperature Sensor Circuit Low Input" "吸気温センサー回路 入力低" "进气温度传感器 输入过低"
emit P0113 "Intake Air Temperature Sensor Circuit High Input" "吸気温センサー回路 入力高" "进气温度传感器 输入过高"
emit P0114 "Intake Air Temperature Sensor Circuit Intermittent" "吸気温センサー回路 断続" "进气温度传感器 间歇故障"
emit P0115 "Engine Coolant Temperature Circuit" "冷却水温センサー回路" "发动机冷却液温度传感器电路"
emit P0116 "Engine Coolant Temperature Circuit Range/Performance" "冷却水温センサー回路 範囲/性能" "冷却液温度传感器 范围/性能"
emit P0117 "Engine Coolant Temperature Circuit Low Input" "冷却水温センサー回路 入力低" "冷却液温度传感器 输入过低"
emit P0118 "Engine Coolant Temperature Circuit High Input" "冷却水温センサー回路 入力高" "冷却液温度传感器 输入过高"
emit P0119 "Engine Coolant Temperature Circuit Intermittent" "冷却水温センサー回路 断続" "冷却液温度传感器 间歇故障"
emit P0120 "Throttle/Pedal Position Sensor A Circuit" "スロットル/ペダル位置センサーA回路" "节气门/踏板位置传感器A电路"
emit P0121 "Throttle/Pedal Position Sensor A Circuit Range/Performance" "スロットル/ペダル位置センサーA 範囲/性能" "节气门/踏板位置传感器A 范围/性能"
emit P0122 "Throttle/Pedal Position Sensor A Circuit Low Input" "スロットル/ペダル位置センサーA 入力低" "节气门/踏板位置传感器A 输入过低"
emit P0123 "Throttle/Pedal Position Sensor A Circuit High Input" "スロットル/ペダル位置センサーA 入力高" "节气门/踏板位置传感器A 输入过高"
emit P0124 "Throttle/Pedal Position Sensor A Circuit Intermittent" "スロットル/ペダル位置センサーA 断続" "节气门/踏板位置传感器A 间歇故障"
emit P0125 "Insufficient Coolant Temperature for Closed Loop Fuel Control" "クローズドループ制御に必要な冷却水温に未到達" "冷却液温度不足 无法进入闭环控制"
emit P0126 "Insufficient Coolant Temperature for Stable Operation" "安定動作に必要な冷却水温に未到達" "冷却液温度不足 运行不稳定"
emit P0128 "Coolant Thermostat Below Regulating Temperature" "サーモスタット 調整温度未満" "节温器低于调节温度"

# ------------------------------------------------- O2 sensor heater circuits
# Blocks of three (circuit / low / high) per bank+sensor.
o2_heater() { # base bank sensor
    b=$2; s=$3
    emit "$(code P $(($1)))" \
        "HO2S Heater Control Circuit (Bank $b Sensor $s)" \
        "O2センサーヒーター制御回路 (バンク$b センサー$s)" \
        "氧传感器加热器控制电路 (第${b}排 传感器$s)"
    emit "$(code P $(($1 + 1)))" \
        "HO2S Heater Control Circuit Low (Bank $b Sensor $s)" \
        "O2センサーヒーター制御回路 低 (バンク$b センサー$s)" \
        "氧传感器加热器控制电路 过低 (第${b}排 传感器$s)"
    emit "$(code P $(($1 + 2)))" \
        "HO2S Heater Control Circuit High (Bank $b Sensor $s)" \
        "O2センサーヒーター制御回路 高 (バンク$b センサー$s)" \
        "氧传感器加热器控制电路 过高 (第${b}排 传感器$s)"
}
o2_heater 30 1 1
o2_heater 36 1 2
o2_heater 42 1 3
o2_heater 50 2 1
o2_heater 56 2 2
o2_heater 62 2 3

# --------------------------------------------------------- O2 sensor circuits
# Blocks of six per bank+sensor, P0130-P0167.
o2_sensor() { # base bank sensor
    b=$2; s=$3
    emit "$(code P $(($1)))" \
        "O2 Sensor Circuit (Bank $b Sensor $s)" \
        "O2センサー回路 (バンク$b センサー$s)" \
        "氧传感器电路 (第${b}排 传感器$s)"
    emit "$(code P $(($1 + 1)))" \
        "O2 Sensor Circuit Low Voltage (Bank $b Sensor $s)" \
        "O2センサー回路 電圧低 (バンク$b センサー$s)" \
        "氧传感器电路 电压过低 (第${b}排 传感器$s)"
    emit "$(code P $(($1 + 2)))" \
        "O2 Sensor Circuit High Voltage (Bank $b Sensor $s)" \
        "O2センサー回路 電圧高 (バンク$b センサー$s)" \
        "氧传感器电路 电压过高 (第${b}排 传感器$s)"
    emit "$(code P $(($1 + 3)))" \
        "O2 Sensor Circuit Slow Response (Bank $b Sensor $s)" \
        "O2センサー回路 応答遅れ (バンク$b センサー$s)" \
        "氧传感器电路 响应迟缓 (第${b}排 传感器$s)"
    emit "$(code P $(($1 + 4)))" \
        "O2 Sensor Circuit No Activity Detected (Bank $b Sensor $s)" \
        "O2センサー回路 活性なし (バンク$b センサー$s)" \
        "氧传感器电路 无活动信号 (第${b}排 传感器$s)"
    emit "$(code P $(($1 + 5)))" \
        "O2 Sensor Heater Circuit (Bank $b Sensor $s)" \
        "O2センサーヒーター回路 (バンク$b センサー$s)" \
        "氧传感器加热器电路 (第${b}排 传感器$s)"
}
o2_sensor 130 1 1
o2_sensor 136 1 2
o2_sensor 142 1 3
o2_sensor 150 2 1
o2_sensor 156 2 2
o2_sensor 162 2 3

# ------------------------------------------------------------- fuel trim etc.
emit P0170 "Fuel Trim Malfunction (Bank 1)" "燃料補正 異常 (バンク1)" "燃油修正故障 (第1排)"
emit P0171 "System Too Lean (Bank 1)" "混合気 薄すぎ (バンク1)" "混合气过稀 (第1排)"
emit P0172 "System Too Rich (Bank 1)" "混合気 濃すぎ (バンク1)" "混合气过浓 (第1排)"
emit P0173 "Fuel Trim Malfunction (Bank 2)" "燃料補正 異常 (バンク2)" "燃油修正故障 (第2排)"
emit P0174 "System Too Lean (Bank 2)" "混合気 薄すぎ (バンク2)" "混合气过稀 (第2排)"
emit P0175 "System Too Rich (Bank 2)" "混合気 濃すぎ (バンク2)" "混合气过浓 (第2排)"
emit P0180 "Fuel Temperature Sensor A Circuit" "燃料温度センサーA回路" "燃油温度传感器A电路"
emit P0181 "Fuel Temperature Sensor A Circuit Range/Performance" "燃料温度センサーA 範囲/性能" "燃油温度传感器A 范围/性能"
emit P0182 "Fuel Temperature Sensor A Circuit Low Input" "燃料温度センサーA 入力低" "燃油温度传感器A 输入过低"
emit P0183 "Fuel Temperature Sensor A Circuit High Input" "燃料温度センサーA 入力高" "燃油温度传感器A 输入过高"
emit P0190 "Fuel Rail Pressure Sensor Circuit" "燃料レール圧センサー回路" "燃油轨压力传感器电路"
emit P0191 "Fuel Rail Pressure Sensor Circuit Range/Performance" "燃料レール圧センサー 範囲/性能" "燃油轨压力传感器 范围/性能"
emit P0192 "Fuel Rail Pressure Sensor Circuit Low Input" "燃料レール圧センサー 入力低" "燃油轨压力传感器 输入过低"
emit P0193 "Fuel Rail Pressure Sensor Circuit High Input" "燃料レール圧センサー 入力高" "燃油轨压力传感器 输入过高"
emit P0194 "Fuel Rail Pressure Sensor Circuit Intermittent" "燃料レール圧センサー 断続" "燃油轨压力传感器 间歇故障"

# ------------------------------------------------------- injector circuits
i=1
while [ $i -le 12 ]; do
    emit "$(code P $((200 + i)))" \
        "Injector Circuit/Open - Cylinder $i" \
        "インジェクター回路/断線 - 気筒$i" \
        "喷油器电路/断路 - 第${i}缸"
    i=$((i + 1))
done

i=1
while [ $i -le 12 ]; do
    base=$((261 + (i - 1) * 3))
    emit "$(code P $base)" \
        "Cylinder $i Injector Circuit Low" \
        "気筒$i インジェクター回路 低" \
        "第${i}缸 喷油器电路过低"
    emit "$(code P $((base + 1)))" \
        "Cylinder $i Injector Circuit High" \
        "気筒$i インジェクター回路 高" \
        "第${i}缸 喷油器电路过高"
    emit "$(code P $((base + 2)))" \
        "Cylinder $i Contribution/Balance" \
        "気筒$i 寄与/バランス" \
        "第${i}缸 贡献度/平衡"
    i=$((i + 1))
done

emit P0217 "Engine Over Temperature Condition" "エンジン オーバーヒート" "发动机温度过高"
emit P0219 "Engine Overspeed Condition" "エンジン オーバーレブ" "发动机超速"
emit P0220 "Throttle/Pedal Position Sensor B Circuit" "スロットル/ペダル位置センサーB回路" "节气门/踏板位置传感器B电路"
emit P0221 "Throttle/Pedal Position Sensor B Circuit Range/Performance" "スロットル/ペダル位置センサーB 範囲/性能" "节气门/踏板位置传感器B 范围/性能"
emit P0222 "Throttle/Pedal Position Sensor B Circuit Low Input" "スロットル/ペダル位置センサーB 入力低" "节气门/踏板位置传感器B 输入过低"
emit P0223 "Throttle/Pedal Position Sensor B Circuit High Input" "スロットル/ペダル位置センサーB 入力高" "节气门/踏板位置传感器B 输入过高"
emit P0230 "Fuel Pump Primary Circuit" "燃料ポンプ 一次回路" "燃油泵主电路"
emit P0231 "Fuel Pump Secondary Circuit Low" "燃料ポンプ 二次回路 低" "燃油泵副电路 过低"
emit P0232 "Fuel Pump Secondary Circuit High" "燃料ポンプ 二次回路 高" "燃油泵副电路 过高"
emit P0234 "Turbocharger/Supercharger Overboost Condition" "過給圧 過大" "增压压力过高"
emit P0299 "Turbocharger/Supercharger Underboost Condition" "過給圧 不足" "增压压力不足"

# --------------------------------------------------------------- misfire
emit P0300 "Random/Multiple Cylinder Misfire Detected" "ランダム/複数気筒 失火検出" "随机/多缸失火"
i=1
while [ $i -le 12 ]; do
    emit "$(code P $((300 + i)))" \
        "Cylinder $i Misfire Detected" \
        "気筒$i 失火検出" \
        "第${i}缸 失火"
    i=$((i + 1))
done
emit P0313 "Misfire Detected with Low Fuel" "燃料残量低下時の失火検出" "低油量时检测到失火"
emit P0314 "Single Cylinder Misfire (Cylinder not Specified)" "単気筒失火 (気筒特定不可)" "单缸失火 (未确定缸号)"
emit P0315 "Crankshaft Position System Variation Not Learned" "クランク角 学習未完了" "曲轴位置系统变差未学习"
emit P0320 "Ignition/Distributor Engine Speed Input Circuit" "点火/ディストリビューター 回転信号回路" "点火/分电器 转速输入电路"
emit P0321 "Ignition/Distributor Engine Speed Input Range/Performance" "点火/ディストリビューター 回転信号 範囲/性能" "点火/分电器 转速输入 范围/性能"
emit P0322 "Ignition/Distributor Engine Speed Input No Signal" "点火/ディストリビューター 回転信号なし" "点火/分电器 无转速信号"
emit P0323 "Ignition/Distributor Engine Speed Input Intermittent" "点火/ディストリビューター 回転信号 断続" "点火/分电器 转速信号间歇"

# ----------------------------------------------------------- knock sensors
knock() { # base sensor bank
    s=$2; b=$3
    emit "$(code P $(($1)))" \
        "Knock Sensor $s Circuit (Bank $b)" \
        "ノックセンサー$s 回路 (バンク$b)" \
        "爆震传感器$s 电路 (第${b}排)"
    emit "$(code P $(($1 + 1)))" \
        "Knock Sensor $s Circuit Range/Performance (Bank $b)" \
        "ノックセンサー$s 範囲/性能 (バンク$b)" \
        "爆震传感器$s 范围/性能 (第${b}排)"
    emit "$(code P $(($1 + 2)))" \
        "Knock Sensor $s Circuit Low Input (Bank $b)" \
        "ノックセンサー$s 入力低 (バンク$b)" \
        "爆震传感器$s 输入过低 (第${b}排)"
    emit "$(code P $(($1 + 3)))" \
        "Knock Sensor $s Circuit High Input (Bank $b)" \
        "ノックセンサー$s 入力高 (バンク$b)" \
        "爆震传感器$s 输入过高 (第${b}排)"
    emit "$(code P $(($1 + 4)))" \
        "Knock Sensor $s Circuit Intermittent (Bank $b)" \
        "ノックセンサー$s 断続 (バンク$b)" \
        "爆震传感器$s 间歇故障 (第${b}排)"
}
knock 325 1 1
knock 330 2 2

# ------------------------------------------------- crank / cam position
pos_block() { # base label_en label_ja label_zh
    emit "$(code P $(($1)))" "$2 Circuit" "$3 回路" "$4 电路"
    emit "$(code P $(($1 + 1)))" "$2 Circuit Range/Performance" "$3 範囲/性能" "$4 范围/性能"
    emit "$(code P $(($1 + 2)))" "$2 Circuit Low Input" "$3 入力低" "$4 输入过低"
    emit "$(code P $(($1 + 3)))" "$2 Circuit High Input" "$3 入力高" "$4 输入过高"
    emit "$(code P $(($1 + 4)))" "$2 Circuit Intermittent" "$3 断続" "$4 间歇故障"
}
pos_block 335 "Crankshaft Position Sensor A" "クランク角センサーA" "曲轴位置传感器A"
pos_block 340 "Camshaft Position Sensor A (Bank 1)" "カム角センサーA (バンク1)" "凸轮轴位置传感器A (第1排)"
pos_block 345 "Camshaft Position Sensor A (Bank 2)" "カム角センサーA (バンク2)" "凸轮轴位置传感器A (第2排)"
pos_block 365 "Camshaft Position Sensor B (Bank 1)" "カム角センサーB (バンク1)" "凸轮轴位置传感器B (第1排)"
pos_block 390 "Camshaft Position Sensor B (Bank 2)" "カム角センサーB (バンク2)" "凸轮轴位置传感器B (第2排)"

# ----------------------------------------------------------- ignition coils
i=1
for letter in A B C D E F G H I J K L; do
    emit "$(code P $((350 + i)))" \
        "Ignition Coil $letter Primary/Secondary Circuit" \
        "イグニッションコイル$letter 一次/二次回路" \
        "点火线圈$letter 初级/次级电路"
    i=$((i + 1))
done
emit P0350 "Ignition Coil Primary/Secondary Circuit" "イグニッションコイル 一次/二次回路" "点火线圈 初级/次级电路"

# ------------------------------------------------------ emission controls
emit P0400 "Exhaust Gas Recirculation Flow" "EGR 流量" "废气再循环流量"
emit P0401 "Exhaust Gas Recirculation Flow Insufficient Detected" "EGR 流量不足" "废气再循环流量不足"
emit P0402 "Exhaust Gas Recirculation Flow Excessive Detected" "EGR 流量過大" "废气再循环流量过大"
emit P0403 "Exhaust Gas Recirculation Circuit" "EGR 回路" "废气再循环电路"
emit P0404 "Exhaust Gas Recirculation Circuit Range/Performance" "EGR 回路 範囲/性能" "废气再循环电路 范围/性能"
emit P0405 "Exhaust Gas Recirculation Sensor A Circuit Low" "EGR センサーA 低" "废气再循环传感器A 过低"
emit P0406 "Exhaust Gas Recirculation Sensor A Circuit High" "EGR センサーA 高" "废气再循环传感器A 过高"
emit P0407 "Exhaust Gas Recirculation Sensor B Circuit Low" "EGR センサーB 低" "废气再循环传感器B 过低"
emit P0408 "Exhaust Gas Recirculation Sensor B Circuit High" "EGR センサーB 高" "废气再循环传感器B 过高"
emit P0410 "Secondary Air Injection System" "二次エア供給システム" "二次空气喷射系统"
emit P0411 "Secondary Air Injection System Incorrect Flow Detected" "二次エア供給 流量異常" "二次空气喷射 流量异常"
emit P0412 "Secondary Air Injection System Switching Valve A Circuit" "二次エア切替バルブA 回路" "二次空气切换阀A 电路"
emit P0413 "Secondary Air Injection System Switching Valve A Circuit Open" "二次エア切替バルブA 回路 断線" "二次空气切换阀A 电路断路"
emit P0414 "Secondary Air Injection System Switching Valve A Circuit Shorted" "二次エア切替バルブA 回路 短絡" "二次空气切换阀A 电路短路"

catalyst() { # base bank
    b=$2
    emit "$(code P $(($1)))" \
        "Catalyst System Efficiency Below Threshold (Bank $b)" \
        "触媒 浄化効率低下 (バンク$b)" \
        "催化器效率低于阈值 (第${b}排)"
    emit "$(code P $(($1 + 1)))" \
        "Warm Up Catalyst Efficiency Below Threshold (Bank $b)" \
        "暖機用触媒 浄化効率低下 (バンク$b)" \
        "起燃催化器效率低于阈值 (第${b}排)"
    emit "$(code P $(($1 + 2)))" \
        "Main Catalyst Efficiency Below Threshold (Bank $b)" \
        "メイン触媒 浄化効率低下 (バンク$b)" \
        "主催化器效率低于阈值 (第${b}排)"
    emit "$(code P $(($1 + 3)))" \
        "Heated Catalyst Efficiency Below Threshold (Bank $b)" \
        "加熱触媒 浄化効率低下 (バンク$b)" \
        "加热催化器效率低于阈值 (第${b}排)"
    emit "$(code P $(($1 + 4)))" \
        "Heated Catalyst Temperature Below Threshold (Bank $b)" \
        "加熱触媒 温度低下 (バンク$b)" \
        "加热催化器温度低于阈值 (第${b}排)"
}
catalyst 420 1
catalyst 430 2

emit P0440 "Evaporative Emission System" "蒸発ガス排出制御システム" "燃油蒸发排放控制系统"
emit P0441 "Evaporative Emission System Incorrect Purge Flow" "蒸発ガス パージ流量異常" "燃油蒸发系统 清污流量异常"
emit P0442 "Evaporative Emission System Leak Detected (Small Leak)" "蒸発ガス 小リーク検出" "燃油蒸发系统 检测到小泄漏"
emit P0443 "Evaporative Emission System Purge Control Valve Circuit" "蒸発ガス パージ制御バルブ回路" "燃油蒸发系统 清污控制阀电路"
emit P0444 "Evaporative Emission System Purge Control Valve Circuit Open" "蒸発ガス パージ制御バルブ 断線" "燃油蒸发系统 清污控制阀断路"
emit P0445 "Evaporative Emission System Purge Control Valve Circuit Shorted" "蒸発ガス パージ制御バルブ 短絡" "燃油蒸发系统 清污控制阀短路"
emit P0446 "Evaporative Emission System Vent Control Circuit" "蒸発ガス ベント制御回路" "燃油蒸发系统 通风控制电路"
emit P0447 "Evaporative Emission System Vent Control Circuit Open" "蒸発ガス ベント制御 断線" "燃油蒸发系统 通风控制断路"
emit P0448 "Evaporative Emission System Vent Control Circuit Shorted" "蒸発ガス ベント制御 短絡" "燃油蒸发系统 通风控制短路"
emit P0449 "Evaporative Emission System Vent Valve/Solenoid Circuit" "蒸発ガス ベントバルブ/ソレノイド回路" "燃油蒸发系统 通风阀/电磁阀电路"
emit P0450 "Evaporative Emission System Pressure Sensor" "蒸発ガス 圧力センサー" "燃油蒸发系统 压力传感器"
emit P0451 "Evaporative Emission System Pressure Sensor Range/Performance" "蒸発ガス 圧力センサー 範囲/性能" "燃油蒸发系统 压力传感器 范围/性能"
emit P0452 "Evaporative Emission System Pressure Sensor Low Input" "蒸発ガス 圧力センサー 入力低" "燃油蒸发系统 压力传感器 输入过低"
emit P0453 "Evaporative Emission System Pressure Sensor High Input" "蒸発ガス 圧力センサー 入力高" "燃油蒸发系统 压力传感器 输入过高"
emit P0454 "Evaporative Emission System Pressure Sensor Intermittent" "蒸発ガス 圧力センサー 断続" "燃油蒸发系统 压力传感器 间歇故障"
emit P0455 "Evaporative Emission System Leak Detected (Gross Leak)" "蒸発ガス 大リーク検出" "燃油蒸发系统 检测到大泄漏"
emit P0456 "Evaporative Emission System Leak Detected (Very Small Leak)" "蒸発ガス 極小リーク検出" "燃油蒸发系统 检测到极小泄漏"
emit P0457 "Evaporative Emission System Leak Detected (Fuel Cap Loose/Off)" "蒸発ガス リーク (給油口キャップ緩み)" "燃油蒸发系统 泄漏 (油箱盖未拧紧)"
emit P0458 "Evaporative Emission System Purge Control Valve Circuit Low" "蒸発ガス パージ制御バルブ 低" "燃油蒸发系统 清污控制阀 过低"
emit P0459 "Evaporative Emission System Purge Control Valve Circuit High" "蒸発ガス パージ制御バルブ 高" "燃油蒸发系统 清污控制阀 过高"
emit P0460 "Fuel Level Sensor Circuit" "燃料レベルセンサー回路" "燃油液位传感器电路"
emit P0461 "Fuel Level Sensor Circuit Range/Performance" "燃料レベルセンサー 範囲/性能" "燃油液位传感器 范围/性能"
emit P0462 "Fuel Level Sensor Circuit Low Input" "燃料レベルセンサー 入力低" "燃油液位传感器 输入过低"
emit P0463 "Fuel Level Sensor Circuit High Input" "燃料レベルセンサー 入力高" "燃油液位传感器 输入过高"
emit P0480 "Fan 1 Control Circuit" "ファン1 制御回路" "冷却风扇1 控制电路"
emit P0481 "Fan 2 Control Circuit" "ファン2 制御回路" "冷却风扇2 控制电路"

# ------------------------------------------------- speed, idle, computer
emit P0500 "Vehicle Speed Sensor A" "車速センサーA" "车速传感器A"
emit P0501 "Vehicle Speed Sensor A Range/Performance" "車速センサーA 範囲/性能" "车速传感器A 范围/性能"
emit P0502 "Vehicle Speed Sensor A Circuit Low Input" "車速センサーA 入力低" "车速传感器A 输入过低"
emit P0503 "Vehicle Speed Sensor A Intermittent/Erratic/High" "車速センサーA 断続/異常/高" "车速传感器A 间歇/异常/过高"
emit P0505 "Idle Air Control System" "アイドル制御システム" "怠速控制系统"
emit P0506 "Idle Air Control System RPM Lower Than Expected" "アイドル回転 低すぎ" "怠速转速低于预期"
emit P0507 "Idle Air Control System RPM Higher Than Expected" "アイドル回転 高すぎ" "怠速转速高于预期"
emit P0510 "Closed Throttle Position Switch" "スロットル全閉スイッチ" "节气门全关位置开关"
emit P0520 "Engine Oil Pressure Sensor/Switch Circuit" "油圧センサー/スイッチ回路" "机油压力传感器/开关电路"
emit P0521 "Engine Oil Pressure Sensor/Switch Range/Performance" "油圧センサー 範囲/性能" "机油压力传感器 范围/性能"
emit P0522 "Engine Oil Pressure Sensor/Switch Low Voltage" "油圧センサー 電圧低" "机油压力传感器 电压过低"
emit P0523 "Engine Oil Pressure Sensor/Switch High Voltage" "油圧センサー 電圧高" "机油压力传感器 电压过高"
emit P0530 "A/C Refrigerant Pressure Sensor A Circuit" "エアコン冷媒圧センサーA 回路" "空调制冷剂压力传感器A 电路"
emit P0532 "A/C Refrigerant Pressure Sensor A Circuit Low" "エアコン冷媒圧センサーA 低" "空调制冷剂压力传感器A 过低"
emit P0533 "A/C Refrigerant Pressure Sensor A Circuit High" "エアコン冷媒圧センサーA 高" "空调制冷剂压力传感器A 过高"
emit P0560 "System Voltage" "システム電圧" "系统电压"
emit P0561 "System Voltage Unstable" "システム電圧 不安定" "系统电压不稳"
emit P0562 "System Voltage Low" "システム電圧 低" "系统电压过低"
emit P0563 "System Voltage High" "システム電圧 高" "系统电压过高"
emit P0571 "Brake Switch A Circuit" "ブレーキスイッチA 回路" "制动开关A 电路"
emit P0600 "Serial Communication Link" "シリアル通信リンク" "串行通信链路"
emit P0601 "Internal Control Module Memory Check Sum Error" "制御モジュール メモリー チェックサム異常" "控制模块存储器校验和错误"
emit P0602 "Control Module Programming Error" "制御モジュール プログラミング異常" "控制模块编程错误"
emit P0603 "Internal Control Module Keep Alive Memory Error" "制御モジュール KAM 異常" "控制模块保持存储器错误"
emit P0604 "Internal Control Module Random Access Memory Error" "制御モジュール RAM 異常" "控制模块RAM错误"
emit P0605 "Internal Control Module Read Only Memory Error" "制御モジュール ROM 異常" "控制模块ROM错误"
emit P0606 "ECM/PCM Processor Fault" "ECM/PCM プロセッサー異常" "ECM/PCM 处理器故障"
emit P0607 "Control Module Performance" "制御モジュール 性能" "控制模块性能"
emit P0620 "Generator Control Circuit" "オルタネーター制御回路" "发电机控制电路"
emit P0625 "Generator Field/F Terminal Circuit Low" "オルタネーター F端子 低" "发电机F端子电路 过低"
emit P0626 "Generator Field/F Terminal Circuit High" "オルタネーター F端子 高" "发电机F端子电路 过高"
emit P0630 "VIN Not Programmed or Incompatible - ECM/PCM" "VIN 未書き込み/不一致 - ECM/PCM" "VIN 未写入或不匹配 - ECM/PCM"
emit P0645 "A/C Clutch Relay Control Circuit" "エアコンクラッチリレー制御回路" "空调离合器继电器控制电路"
emit P0650 "Malfunction Indicator Lamp Control Circuit" "MIL 制御回路" "故障指示灯控制电路"
emit P0685 "ECM/PCM Power Relay Control Circuit /Open" "ECM/PCM 電源リレー制御回路/断線" "ECM/PCM 电源继电器控制电路/断路"

# ------------------------------------------------------------ transmission
emit P0700 "Transmission Control System (MIL Request)" "変速機制御システム (MIL要求)" "变速器控制系统 (请求点亮MIL)"
emit P0701 "Transmission Control System Range/Performance" "変速機制御システム 範囲/性能" "变速器控制系统 范围/性能"
emit P0702 "Transmission Control System Electrical" "変速機制御システム 電気系" "变速器控制系统 电气故障"
emit P0703 "Torque Converter/Brake Switch B Circuit" "トルコン/ブレーキスイッチB 回路" "变矩器/制动开关B 电路"
emit P0705 "Transmission Range Sensor Circuit (PRNDL Input)" "シフト位置センサー回路 (PRNDL)" "变速器挡位传感器电路 (PRNDL)"
emit P0706 "Transmission Range Sensor Circuit Range/Performance" "シフト位置センサー 範囲/性能" "变速器挡位传感器 范围/性能"
emit P0707 "Transmission Range Sensor Circuit Low Input" "シフト位置センサー 入力低" "变速器挡位传感器 输入过低"
emit P0708 "Transmission Range Sensor Circuit High Input" "シフト位置センサー 入力高" "变速器挡位传感器 输入过高"
pos_block 710 "Transmission Fluid Temperature Sensor A" "ATF温度センサーA" "变速器油温传感器A"
pos_block 715 "Input/Turbine Speed Sensor A" "インプット/タービン回転センサーA" "输入轴/涡轮转速传感器A"
pos_block 720 "Output Speed Sensor" "アウトプット回転センサー" "输出轴转速传感器"
pos_block 725 "Engine Speed Input Circuit" "エンジン回転入力" "发动机转速输入"
emit P0730 "Incorrect Gear Ratio" "ギヤ比 異常" "挡位传动比不正确"
i=1
while [ $i -le 5 ]; do
    emit "$(code P $((730 + i)))" \
        "Gear $i Incorrect Ratio" \
        "$i 速 ギヤ比異常" \
        "第${i}挡 传动比不正确"
    i=$((i + 1))
done
emit P0736 "Reverse Incorrect Ratio" "リバース ギヤ比異常" "倒挡传动比不正确"
emit P0740 "Torque Converter Clutch Circuit" "ロックアップクラッチ回路" "变矩器锁止离合器电路"
emit P0741 "Torque Converter Clutch Circuit Performance/Stuck Off" "ロックアップクラッチ 性能/解放固着" "变矩器离合器 性能/卡在分离"
emit P0742 "Torque Converter Clutch Circuit Stuck On" "ロックアップクラッチ 係合固着" "变矩器离合器 卡在结合"
emit P0743 "Torque Converter Clutch Circuit Electrical" "ロックアップクラッチ 電気系" "变矩器离合器 电气故障"
emit P0744 "Torque Converter Clutch Circuit Intermittent" "ロックアップクラッチ 断続" "变矩器离合器 间歇故障"

solenoid() { # base letter
    l=$2
    emit "$(code P $(($1)))" \
        "Shift Solenoid $l" \
        "シフトソレノイド$l" \
        "换挡电磁阀$l"
    emit "$(code P $(($1 + 1)))" \
        "Shift Solenoid $l Performance or Stuck Off" \
        "シフトソレノイド$l 性能/OFF固着" \
        "换挡电磁阀$l 性能或卡在关闭"
    emit "$(code P $(($1 + 2)))" \
        "Shift Solenoid $l Stuck On" \
        "シフトソレノイド$l ON固着" \
        "换挡电磁阀$l 卡在开启"
    emit "$(code P $(($1 + 3)))" \
        "Shift Solenoid $l Electrical" \
        "シフトソレノイド$l 電気系" \
        "换挡电磁阀$l 电气故障"
    emit "$(code P $(($1 + 4)))" \
        "Shift Solenoid $l Intermittent" \
        "シフトソレノイド$l 断続" \
        "换挡电磁阀$l 间歇故障"
}
solenoid 750 A
solenoid 755 B
solenoid 760 C
solenoid 765 D
solenoid 770 E
emit P0745 "Pressure Control Solenoid A" "プレッシャーコントロールソレノイドA" "压力控制电磁阀A"
emit P0746 "Pressure Control Solenoid A Performance or Stuck Off" "プレッシャーコントロールソレノイドA 性能/OFF固着" "压力控制电磁阀A 性能或卡在关闭"
emit P0747 "Pressure Control Solenoid A Stuck On" "プレッシャーコントロールソレノイドA ON固着" "压力控制电磁阀A 卡在开启"
emit P0748 "Pressure Control Solenoid A Electrical" "プレッシャーコントロールソレノイドA 電気系" "压力控制电磁阀A 电气故障"
emit P0749 "Pressure Control Solenoid A Intermittent" "プレッシャーコントロールソレノイドA 断続" "压力控制电磁阀A 间歇故障"
emit P0780 "Shift Malfunction" "変速 異常" "换挡故障"
emit P0781 "1-2 Shift Malfunction" "1-2速 変速異常" "1-2挡 换挡故障"
emit P0782 "2-3 Shift Malfunction" "2-3速 変速異常" "2-3挡 换挡故障"
emit P0783 "3-4 Shift Malfunction" "3-4速 変速異常" "3-4挡 换挡故障"
emit P0784 "4-5 Shift Malfunction" "4-5速 変速異常" "4-5挡 换挡故障"

# -------------------------------------------------------- network / U codes
emit U0001 "High Speed CAN Communication Bus" "高速CAN通信バス" "高速CAN通信总线"
emit U0002 "High Speed CAN Communication Bus Performance" "高速CAN通信バス 性能" "高速CAN通信总线 性能"
emit U0003 "High Speed CAN Communication Bus (+) Open" "高速CAN通信バス (+) 断線" "高速CAN通信总线 (+) 断路"
emit U0004 "High Speed CAN Communication Bus (+) Low" "高速CAN通信バス (+) 低" "高速CAN通信总线 (+) 过低"
emit U0005 "High Speed CAN Communication Bus (+) High" "高速CAN通信バス (+) 高" "高速CAN通信总线 (+) 过高"
emit U0010 "Medium Speed CAN Communication Bus" "中速CAN通信バス" "中速CAN通信总线"
emit U0073 "Control Module Communication Bus A Off" "制御モジュール通信バスA オフ" "控制模块通信总线A 关闭"
emit U0074 "Control Module Communication Bus B Off" "制御モジュール通信バスB オフ" "控制模块通信总线B 关闭"

# lost_comm <number> <module_en> <module_ja> <module_zh>
lost_comm() {
    emit "$(code U $1)" \
        "Lost Communication With $2" \
        "$3 との通信途絶" \
        "与${4}通信丢失"
}
lost_comm 100 "ECM/PCM A" "エンジン制御モジュールA" "发动机控制模块A"
lost_comm 101 "Transmission Control Module" "変速機制御モジュール" "变速器控制模块"
lost_comm 102 "Transfer Case Control Module" "トランスファー制御モジュール" "分动箱控制模块"
lost_comm 103 "Gear Shift Module" "シフト制御モジュール" "换挡控制模块"
lost_comm 104 "Cruise Control Module" "クルーズコントロールモジュール" "巡航控制模块"
lost_comm 105 "Fuel Injector Control Module" "インジェクター制御モジュール" "喷油器控制模块"
lost_comm 106 "Glow Plug Control Module" "グロープラグ制御モジュール" "电热塞控制模块"
lost_comm 107 "Throttle Actuator Control Module" "スロットルアクチュエーター制御モジュール" "节气门执行器控制模块"
lost_comm 109 "Fuel Pump Control Module" "燃料ポンプ制御モジュール" "燃油泵控制模块"
lost_comm 110 "Drive Motor Control Module" "駆動モーター制御モジュール" "驱动电机控制模块"
lost_comm 111 "Battery Energy Control Module A" "バッテリーエネルギー制御モジュールA" "电池能量控制模块A"
lost_comm 121 "Anti-Lock Brake System Control Module" "ABS制御モジュール" "ABS控制模块"
lost_comm 122 "Vehicle Dynamics Control Module" "車両姿勢制御モジュール" "车辆动态控制模块"
lost_comm 123 "Yaw Rate Sensor Module" "ヨーレートセンサーモジュール" "横摆角速度传感器模块"
lost_comm 124 "Lateral Acceleration Sensor Module" "横加速度センサーモジュール" "侧向加速度传感器模块"
lost_comm 126 "Steering Angle Sensor Module" "舵角センサーモジュール" "转向角传感器模块"
lost_comm 128 "Park Brake Control Module" "パーキングブレーキ制御モジュール" "驻车制动控制模块"
lost_comm 129 "Brake System Control Module" "ブレーキシステム制御モジュール" "制动系统控制模块"
lost_comm 131 "Power Steering Control Module" "パワーステアリング制御モジュール" "动力转向控制模块"
lost_comm 140 "Body Control Module" "ボディ制御モジュール" "车身控制模块"
lost_comm 151 "Restraints Control Module" "エアバッグ制御モジュール" "安全气囊控制模块"
lost_comm 155 "Instrument Panel Cluster Control Module" "メーター制御モジュール" "仪表控制模块"
lost_comm 164 "HVAC Control Module" "エアコン制御モジュール" "空调控制模块"
lost_comm 167 "Vehicle Immobilizer Control Module" "イモビライザー制御モジュール" "防盗控制模块"
lost_comm 184 "Radio" "オーディオ" "音响主机"
lost_comm 199 "Door Control Module A" "ドア制御モジュールA" "车门控制模块A"
lost_comm 208 "Seat Control Module A" "シート制御モジュールA" "座椅控制模块A"
lost_comm 212 "Steering Column Control Module" "ステアリングコラム制御モジュール" "转向柱控制模块"
lost_comm 214 "Occupant Classification System Module" "乗員判別システムモジュール" "乘员分类系统模块"

emit U0300 "Internal Control Module Software Incompatibility" "制御モジュール ソフトウェア不整合" "控制模块软件不兼容"
emit U0301 "Software Incompatibility with ECM/PCM" "ECM/PCM ソフトウェア不整合" "与ECM/PCM软件不兼容"
emit U0302 "Software Incompatibility with Transmission Control Module" "変速機制御モジュール ソフトウェア不整合" "与变速器控制模块软件不兼容"

# invalid_data <number> <module_en> <module_ja> <module_zh>
invalid_data() {
    emit "$(code U $1)" \
        "Invalid Data Received From $2" \
        "$3 から不正データ受信" \
        "从${4}接收到无效数据"
}
invalid_data 401 "ECM/PCM A" "エンジン制御モジュールA" "发动机控制模块A"
invalid_data 402 "Transmission Control Module" "変速機制御モジュール" "变速器控制模块"
invalid_data 415 "Anti-Lock Brake System Control Module" "ABS制御モジュール" "ABS控制模块"
invalid_data 416 "Vehicle Dynamics Control Module" "車両姿勢制御モジュール" "车辆动态控制模块"
invalid_data 422 "Body Control Module" "ボディ制御モジュール" "车身控制模块"
invalid_data 447 "Gateway A" "ゲートウェイA" "网关A"

# ---------------------------------------------------- P2xxx generic codes
emit P2000 "NOx Trap Efficiency Below Threshold (Bank 1)" "NOxトラップ 効率低下 (バンク1)" "氮氧化物捕集器效率低于阈值 (第1排)"
emit P2002 "Diesel Particulate Filter Efficiency Below Threshold (Bank 1)" "DPF 捕集効率低下 (バンク1)" "颗粒捕集器效率低于阈值 (第1排)"
emit P2004 "Intake Manifold Runner Control Stuck Open (Bank 1)" "吸気マニホールド可変制御 開固着 (バンク1)" "进气歧管调节控制 卡在开启 (第1排)"
emit P2006 "Intake Manifold Runner Control Stuck Closed (Bank 1)" "吸気マニホールド可変制御 閉固着 (バンク1)" "进气歧管调节控制 卡在关闭 (第1排)"
emit P2015 "Intake Manifold Runner Position Sensor Circuit Range/Performance (Bank 1)" "吸気マニホールド位置センサー 範囲/性能 (バンク1)" "进气歧管位置传感器 范围/性能 (第1排)"
emit P2096 "Post Catalyst Fuel Trim System Too Lean (Bank 1)" "触媒後 燃料補正 薄すぎ (バンク1)" "催化器后燃油修正过稀 (第1排)"
emit P2097 "Post Catalyst Fuel Trim System Too Rich (Bank 1)" "触媒後 燃料補正 濃すぎ (バンク1)" "催化器后燃油修正过浓 (第1排)"
emit P2101 "Throttle Actuator A Control Motor Circuit Range/Performance" "スロットルアクチュエーターA 範囲/性能" "节气门执行器A 范围/性能"
emit P2119 "Throttle Actuator A Control Throttle Body Range/Performance" "スロットルボディA 範囲/性能" "节气门体A 范围/性能"
emit P2122 "Throttle/Pedal Position Sensor D Circuit Low Input" "スロットル/ペダル位置センサーD 入力低" "节气门/踏板位置传感器D 输入过低"
emit P2123 "Throttle/Pedal Position Sensor D Circuit High Input" "スロットル/ペダル位置センサーD 入力高" "节气门/踏板位置传感器D 输入过高"
emit P2127 "Throttle/Pedal Position Sensor E Circuit Low Input" "スロットル/ペダル位置センサーE 入力低" "节气门/踏板位置传感器E 输入过低"
emit P2128 "Throttle/Pedal Position Sensor E Circuit High Input" "スロットル/ペダル位置センサーE 入力高" "节气门/踏板位置传感器E 输入过高"
emit P2135 "Throttle/Pedal Position Sensor A/B Voltage Correlation" "スロットル/ペダル位置センサーA/B 電圧相関" "节气门/踏板位置传感器A/B 电压相关性"
emit P2138 "Throttle/Pedal Position Sensor D/E Voltage Correlation" "スロットル/ペダル位置センサーD/E 電圧相関" "节气门/踏板位置传感器D/E 电压相关性"
emit P2187 "System Too Lean at Idle (Bank 1)" "アイドル時 混合気 薄すぎ (バンク1)" "怠速时混合气过稀 (第1排)"
emit P2188 "System Too Rich at Idle (Bank 1)" "アイドル時 混合気 濃すぎ (バンク1)" "怠速时混合气过浓 (第1排)"
emit P2195 "O2 Sensor Signal Stuck Lean (Bank 1 Sensor 1)" "O2センサー信号 リーン固着 (バンク1 センサー1)" "氧传感器信号卡在稀 (第1排 传感器1)"
emit P2196 "O2 Sensor Signal Stuck Rich (Bank 1 Sensor 1)" "O2センサー信号 リッチ固着 (バンク1 センサー1)" "氧传感器信号卡在浓 (第1排 传感器1)"
emit P2270 "O2 Sensor Signal Stuck Lean (Bank 1 Sensor 2)" "O2センサー信号 リーン固着 (バンク1 センサー2)" "氧传感器信号卡在稀 (第1排 传感器2)"
emit P2271 "O2 Sensor Signal Stuck Rich (Bank 1 Sensor 2)" "O2センサー信号 リッチ固着 (バンク1 センサー2)" "氧传感器信号卡在浓 (第1排 传感器2)"
emit P2279 "Intake Air System Leak" "吸気系 リーク" "进气系统泄漏"
emit P2413 "Exhaust Gas Recirculation System Performance" "EGRシステム 性能" "废气再循环系统性能"
emit P2463 "Diesel Particulate Filter Restriction - Soot Accumulation" "DPF 詰まり - すす堆積" "颗粒捕集器堵塞 - 碳烟堆积"

# -------------------------------------------------------- chassis / C codes
emit C0035 "Left Front Wheel Speed Sensor Circuit" "左前輪速センサー回路" "左前轮速传感器电路"
emit C0040 "Right Front Wheel Speed Sensor Circuit" "右前輪速センサー回路" "右前轮速传感器电路"
emit C0045 "Left Rear Wheel Speed Sensor Circuit" "左後輪速センサー回路" "左后轮速传感器电路"
emit C0050 "Right Rear Wheel Speed Sensor Circuit" "右後輪速センサー回路" "右后轮速传感器电路"
emit C0051 "Steering Wheel Position Sensor Circuit" "ステアリング舵角センサー回路" "方向盘位置传感器电路"
emit C0110 "Pump Motor Circuit" "ポンプモーター回路" "泵电机电路"
emit C0121 "Valve Relay Circuit" "バルブリレー回路" "阀继电器电路"
emit C0265 "EBCM Relay Circuit" "EBCM リレー回路" "EBCM 继电器电路"
emit C0550 "Electronic Control Unit Performance" "電子制御ユニット 性能" "电子控制单元性能"

# ----------------------------------------------------------- body / B codes
emit B0001 "Driver Frontal Stage 1 Deployment Control" "運転席フロントエアバッグ 第1段 展開制御" "驾驶席正面第1级气囊展开控制"
emit B0002 "Driver Frontal Stage 2 Deployment Control" "運転席フロントエアバッグ 第2段 展開制御" "驾驶席正面第2级气囊展开控制"
emit B0010 "Passenger Frontal Stage 1 Deployment Control" "助手席フロントエアバッグ 第1段 展開制御" "副驾正面第1级气囊展开控制"
emit B0011 "Passenger Frontal Stage 2 Deployment Control" "助手席フロントエアバッグ 第2段 展開制御" "副驾正面第2级气囊展开控制"

# ------------------------------------------------------------------ finish
sort -o "$EN" "$EN"
sort -o "$JA" "$JA"
sort -o "$ZH" "$ZH"

printf 'en: %s codes\n' "$(grep -c '' "$EN")"
printf 'ja: %s codes\n' "$(grep -c '' "$JA")"
printf 'zh: %s codes\n' "$(grep -c '' "$ZH")"
