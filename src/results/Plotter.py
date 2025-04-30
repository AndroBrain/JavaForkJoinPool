import pandas as pd
import matplotlib.pyplot as plt
import io
import seaborn as sns

# Dane CSV jako string
csv_data = """Threshold,Parallelism,Steals,TimeMillis
100,11,36,3992
100,12,37,3245
100,1,1,13992
100,2,2,7332
100,4,4,4279
100,8,28,3308
100,16,69,2724
100,32,232,2799
100,64,368,2641
100,128,925,2774
10,11,84,3128
10,12,55,3055
10,1,1,15666
10,2,2,8224
10,4,6,4980
10,8,18,3622
10,16,45,3186
10,32,252,2891
10,64,427,3077
10,128,633,3173
1,11,123,4503
1,12,45,3914
1,1,1,21236
1,2,2,11523
1,4,8,6669
1,8,29,5041
1,16,133,4101
1,32,282,4032
1,64,552,4103
1,128,1312,3968
"""

# Wczytanie danych do DataFrame
df = pd.read_csv(io.StringIO(csv_data))

# Konwersja kolumny Threshold na typ kategorialny lub string dla lepszego grupowania na wykresie
df['Threshold'] = df['Threshold'].astype(str)

# Wykres 1: Czas wykonania vs Poziom równoległości dla różnych progów (podobny do przykładu)
plt.figure(figsize=(12, 6))
sns.lineplot(data=df, x='Parallelism', y='TimeMillis', hue='Threshold', marker='o')
plt.title('Parallelism vs time in millis')
plt.xlabel('Parallelism')
plt.ylabel('Time in millis')
plt.grid(True)
plt.xscale('log', base=2) # Skala logarytmiczna dla lepszego rozłożenia punktów na osi X
plt.xticks([1, 2, 4, 8, 16, 32, 64, 128], labels=[1, 2, 4, 8, 16, 32, 64, 128]) # Ustawienie konkretnych znaczników
plt.show()

# Wykres 2: Liczba "Steals" vs Poziom równoległości dla różnych progów
plt.figure(figsize=(12, 6))
sns.lineplot(data=df, x='Parallelism', y='Steals', hue='Threshold', marker='o')
plt.title('Steals vs Parallelism')
plt.xlabel('Parallelism')
plt.ylabel('Steals')
plt.grid(True)
plt.xscale('log', base=2) # Skala logarytmiczna dla lepszego rozłożenia punktów na osi X
plt.xticks([1, 2, 4, 8, 16, 32, 64, 128], labels=[1, 2, 4, 8, 16, 32, 64, 128]) # Ustawienie konkretnych znaczników
plt.show()

# Wykres 3: Czas wykonania vs Liczba "Steals" dla różnych progów
# Ten wykres może być mniej czytelny, ale pokazuje potencjalną korelację
plt.figure(figsize=(12, 6))
sns.lineplot(data=df, x='Steals', y='TimeMillis', hue='Threshold', marker='o') # Używamy scatterplot dla punktów
plt.title('Time in millis vs steals')
plt.xlabel('Steals')
plt.ylabel('Time in millis')
plt.grid(True)
plt.show()
