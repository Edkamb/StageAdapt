import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from scipy.interpolate import griddata

# Load data from CSV files
t1_data = pd.read_csv('sem.csv')
t2_data = pd.read_csv('decl.csv')

# Extract X, Y, and T values
x1, y1, t1 = t1_data['n'], t1_data['m'], t1_data['T']
x2, y2, t2 = t2_data['n'], t2_data['m'], t2_data['T']

# Define grid for interpolation
#num_points = 6  # Adjust this number for grid density
num_points = 50  # Adjust this number for grid density
xi1 = np.linspace(x1.min(), x1.max(), num_points)
yi1 = np.linspace(y1.min(), y1.max(), num_points)
xi2 = np.linspace(x2.min(), x2.max(), num_points)
yi2 = np.linspace(y2.min(), y2.max(), num_points)

# Create meshgrid
X1, Y1 = np.meshgrid(xi1, yi1)
X2, Y2 = np.meshgrid(xi2, yi2)

# Interpolate data onto grid
T1 = griddata((x1, y1), t1, (X1, Y1), method='cubic')
T2 = griddata((x2, y2), t2, (X2, Y2), method='cubic')

# Create a 3D plot
fig = plt.figure(figsize=(12, 8))
ax = fig.add_subplot(111, projection='3d')

# 3D Wireframe plot for T1
wire1 = ax.plot_wireframe(X1, Y1, T1, color='blue', alpha=0.5, label='semantic')

# 3D Wireframe plot for T2
wire2 = ax.plot_wireframe(X2, Y2, T2, color='red', alpha=0.5, label='non-semantic')

# Set labels and title
ax.set_xlabel('n (stages)', fontsize=14)
ax.set_ylabel('m (objects)', fontsize=14)
ax.set_zlabel('t in s', fontsize=14)

ax.tick_params(axis='both', which='major', labelsize=12)
ax.tick_params(axis='both', which='minor', labelsize=12)

# Set axis limits
ax.set_xlim(min(x1.min(), x2.min()), max(x1.max(), x2.max()))
ax.set_ylim(min(y1.min(), y2.min()), max(y1.max(), y2.max()))

# Add a legend
ax.legend()

# Display the plot
plt.tight_layout()
plt.show()
