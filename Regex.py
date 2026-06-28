import re

with open("log_transiciones.txt", "r", encoding="utf-8", errors="replace") as f:
    original = f.read().strip()

pattern = r'T0-(.*?)T1-(.*?)(T2-(.*?)T3-(.*?)T4-|(T5-(.*?)T6-)|(T7-(.*?)T8-(.*?)T9-(.*?)T10-))(.*?)T11-'
reemplazo = r'\g<1>\g<2>\g<4>\g<5>\g<7>\g<9>\g<10>\g<11>\g<12>'

restante = original
cantidad_total = 0
while True:
    restante, cantidad = re.subn(pattern, reemplazo, restante)
    cantidad_total += cantidad
    if cantidad == 0:
        break

print(f"Invariantes de transicion encontrados: {cantidad_total}")

with open("log_transiciones.txt", "w", encoding="utf-8") as f:
    f.write(restante)

if restante == "":
    print("OK - No sobra texto. Todos los disparos pertenecen a invariantes validos.")
else:
    print("ERROR - Sobra texto que no forma invariantes validos. Revisa log_transiciones.txt:")
    print(restante)
