import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

df = pd.read_csv("Prueba.csv")



#create boxplot by group
sns.boxplot(data=df, 
            x = df['ventana'],
            y = df['valor'],
            hue = df['algoritmo'], 
            
            )
sns.set_theme(style="darkgrid")
#sns.despine(offset=10, trim=True)
plt.show()