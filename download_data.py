import matplotlib.pyplot as plt
import numpy as np
import PIL
import tensorflow as tf

from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras.models import Sequential

# Install dependencies as needed:
# pip install kagglehub[pandas-datasets]
import kagglehub
from kagglehub import KaggleDatasetAdapter

# Download latest version
kagglehub.dataset_download("moazeldsokyx/dogs-vs-cats")
kagglehub.dataset_download("atulanandjha/lfwpeople")
kagglehub.dataset_download("stealthtechnologies/birds-images-dataset")
kagglehub.dataset_download("mohamedgobara/26-class-object-detection-dataset")
kagglehub.dataset_download("rahmasleam/flowers-dataset")


print("Download complete")