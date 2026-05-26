import re

with open("log_estadisticas.txt", "r", encoding="utf-8", errors="replace") as f:
    logs = f.read()
    logs = logs.split('##########')[0]
    print(logs)
    #logs  ='T0-T1-T7-T0-T8-T1-T9-T10-T11-'
    pattern = r'T0-(.*?)T1-(.*?)(T2-(.*?)T3-(.*?)T4-|(T5-(.*?)T6-)|(T7-(.*?)T8-(.*?)T9-(.*?)T10-))(.*?)T11-'
    reemplazo = r'\g<1>\g<2>\g<4>\g<5>\g<7>\g<9>\g<10>\g<11>\g<12>'

    cantidad_total = 0
    while True:
        line, cantidad = re.subn(pattern, reemplazo, logs)
        #print(line)
        cantidad_total += cantidad
        logs = line
        if cantidad == 0:
            break

    print(f"Total de reemplazos: {cantidad_total}")
    print("Texto sobrante:")
    print(logs)


