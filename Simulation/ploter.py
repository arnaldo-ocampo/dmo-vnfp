from autograd import numpy
from numpy.lib.shape_base import tile
from pymoo.factory import get_problem, get_reference_directions
from pymoo.visualization.pcp import PCP
from collections import OrderedDict
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib import cm
from matplotlib.ticker import ScalarFormatter
import sys
import os
import locale

locale.setlocale(locale.LC_ALL, 'pt_BR.UTF-8')

colors = ['#BF3B53', '#D2D6D9', '#8C4D3F', '#BF7969', '#F2B5A7', '#263140', '#F2E9D8', '#F29D52', '#D97652', '#A6523F']

plt.rc('legend', fontsize=8)    # legend fontsize

def format_data(value):

    if value.is_integer():
        return '{0:n}'.format(value)

    return '{0:n}'.format(round(value, 2))
        

def generate_plot(file_name, normalice=True):
    title_plot = file_name.replace(".csv", "")
    output_name = title_plot + ".png"
    bounds = True

    if not normalice:
        title_plot = title_plot + " (Normalizado)"
        output_name = title_plot + ".png"
        bounds = False

    data = pd.read_csv(file_name, dtype='float')
    rows = data.to_numpy()
    plot = PCP( title=(title_plot.replace("_", " "), {'pad': 30}), 
                show_bounds=True, 
                figsize=(9, 5),
                labels=data.columns.values.tolist(),
                legend=(True, {'loc': "right", "bbox_to_anchor":(1.06, 0.5)}),
                normalize_each_axis=normalice,
                #axis_style={"font.size":6},
                #bbox=True,
                func_number_to_text=format_data
                )
    index = 0
    while index < len(rows):
        plot.add(rows[index], color=colors[index] ,label="R" + str(index+1))
        index +=1

    #plot.show()
    plot.save(os.path.join("img", output_name))



def generate_box_diagram(file_name):

    data = pd.read_csv(file_name, dtype='float')

    fig = plt.figure(figsize =(6, 4)) 
    ax = fig.add_subplot() 
    
    bp = ax.boxplot(data, showfliers=False) 
    
    colors = ['#0000FF', '#00FF00',  '#FFFF00', '#FF00FF'] 
    
    for whisker in bp['whiskers']: 
        whisker.set(color ='#8B008B', linewidth = 1, linestyle =":") 
    
    for cap in bp['caps']: 
        cap.set(color ='#8B008B', 
                linewidth = 1) 
    
    for median in bp['medians']: 
        median.set(color ='red', 
                linewidth = 1.5) 
    
    for flier in bp['fliers']: 
        flier.set(marker ='D', 
                color ='#e7298a', 
                alpha = 0.5) 

    ax.set_xticklabels(data.columns.values.tolist()) 
    ax.grid(color='grey', axis='y', linestyle='-', linewidth=0.25, alpha=0.5)

    plt.title(file_name.replace(".csv", "").replace("_", " ") ) 
    
    ax.get_xaxis().tick_bottom() 
    ax.get_yaxis().tick_left() 
        
    #plt.show()
    plt.savefig(os.path.join("img", file_name.replace(".csv", ".png")))


################################### MAIN #######################################

files = [f for f in os.listdir() if f.endswith('.csv')]

#generate_plot("F1_DNSGA_A2.csv")
#generate_box_diagram("Ventana_#0_-_Costo_de_Energia.csv")



for file_name in files:
    if 'Ventana' in file_name:
        generate_box_diagram(file_name)
    else:    
        generate_plot(file_name, False)
