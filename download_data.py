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
#kagglehub.dataset_download("moazeldsokyx/dogs-vs-cats", output_dir="Dataset/dogsvscats")
#kagglehub.dataset_download("stealthtechnologies/birds-images-dataset", output_dir="Dataset/birds")
#kagglehub.dataset_download("rahmasleam/flowers-dataset", output_dir="Dataset/flowers")
#kagglehub.dataset_download("sbaghbidi/human-faces-object-detection", output_dir="Dataset/Faces")
#kagglehub.dataset_download("andrewteplov/car-object-detection", output_dir="Dataset/Cars")
#kagglehub.dataset_download("issaisasank/guns-object-detection", output_dir="Dataset/Guns")
#kagglehub.dataset_download("mbkinaci/fruit-images-for-object-detection", output_dir="Dataset/Fruit")

#/home/masonc/Capstone/CMSC-495-Capstone-Project/Dataset





print("Download complete")