import urllib.request
import numpy as np
import os
from PIL import Image

# we can check the official list here: https://github.com/googlecreativelab/quickdraw-dataset/blob/master/categories.txt
categories = ['apple', 'banana', 'baseball', 'bee', 'bird', 'butterfly', 'camera', 'car', 'cat', 'circle', 'clock', 'cloud', 'cookie', 'dog', 'donut', 'duck', 'envelope', 'eye', 'eyeglasses', 'face', 'fish', 'flower', 'foot', 'fork', 'frog', 'giraffe', 'grapes', 'guitar', 'hand', 'hat', 'horse', 'house', 'ice cream', 'leaf', 'leg', 'light bulb', 'lighthouse', 'lightning', 'lollipop', 'moon', 'mountain', 'octopus', 'palm tree', 'pants', 'peanut', 'scissors', 'shark', 'shoe', 'shovel', 'smiley face', 'snail', 'snowflake', 'snowman', 'square', 'star', 'strawberry', 'sun', 'sword', 'tooth', 'tree', 'triangle', 'wheel', 'windmill']

base_url = "https://storage.googleapis.com/quickdraw_dataset/full/numpy_bitmap/"
dataset_dir = "Dataset"
os.makedirs(dataset_dir, exist_ok=True)

# 2000 images per category is plenty to get high accuracy without taking hours to train
images_per_category = 2000 

for cat in categories:
    print(f"Downloading {cat} data...")
    # Handle spaces in category names (e.g., 'ice cream' -> 'ice%20cream')
    url = base_url + cat.replace(" ", "%20") + ".npy"
    file_path = f"{cat}.npy"
    urllib.request.urlretrieve(url, file_path)
    
    print(f"Extracting and converting {cat} to PNGs...")
    data = np.load(file_path)
    
    cat_dir = os.path.join(dataset_dir, cat)
    os.makedirs(cat_dir, exist_ok=True)
    
    for i in range(min(images_per_category, len(data))):
        # QuickDraw arrays are 1D, we need to reshape them to 28x28 squares
        img_array = data[i].reshape(28, 28)
        
        # CRITICAL FIX: QuickDraw natively saves drawings as white lines on a black background.
        # We invert it (255 - pixel) so they become black lines on a white background 
        # to perfectly match the drawings you will test it with.
        img_array = 255 - img_array 
        
        img = Image.fromarray(img_array.astype(np.uint8))
        img.save(os.path.join(cat_dir, f"{cat}_{i}.png"))
        
    # Delete the massive .npy file to save hard drive space
    os.remove(file_path) 

print(f"Success. Path:'{dataset_dir}'")