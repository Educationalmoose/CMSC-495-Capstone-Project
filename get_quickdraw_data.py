import urllib.request
import numpy as np
import os
from PIL import Image

categories = ['apple', 'banana', 'baseball', 'bee', 'bird', 'butterfly', 'camera', 'car', 'cat', 'circle', 'clock', 'cloud', 'cookie', 'dog', 'donut', 'duck', 'envelope', 'eye', 'eyeglasses', 'face', 'fish', 'flower', 'foot', 'fork', 'frog', 'giraffe', 'grapes', 'guitar', 'hand', 'hat', 'horse', 'house', 'ice cream', 'leaf', 'leg', 'light bulb', 'lighthouse', 'lightning', 'lollipop', 'moon', 'mountain', 'octopus', 'palm tree', 'pants', 'peanut', 'scissors', 'shark', 'shoe', 'shovel', 'smiley face', 'snail', 'snowflake', 'snowman', 'square', 'star', 'strawberry', 'sun', 'sword', 'tooth', 'tree', 'triangle', 'wheel', 'windmill']

base_url = "https://storage.googleapis.com/quickdraw_dataset/full/numpy_bitmap/"
dataset_dir = "Dataset"

train_dir = os.path.join(dataset_dir, "Training")
test_dir = os.path.join(dataset_dir, "Testing")

os.makedirs(train_dir, exist_ok=True)
os.makedirs(test_dir, exist_ok=True)

train_amount = 5000
test_amount = 1000

for cat in categories:
    print(f"Downloading {cat} data...")
    # handle spaces in category names
    url = base_url + cat.replace(" ", "%20") + ".npy"
    file_path = f"{cat}.npy"
    urllib.request.urlretrieve(url, file_path)
    
    print(f"Extracting and organizing {cat}...")
    data = np.load(file_path)
    
    cat_train_dir = os.path.join(train_dir, cat)
    cat_test_dir = os.path.join(test_dir, cat)
    os.makedirs(cat_train_dir, exist_ok=True)
    os.makedirs(cat_test_dir, exist_ok=True)
    
    # process training images 
    for i in range(min(train_amount, len(data))):
        img_array = data[i].reshape(28, 28)
        img_array = 255 - img_array # invert to black lines on white background to match the format of the drawings
        
        img = Image.fromarray(img_array.astype(np.uint8))
        img.save(os.path.join(cat_train_dir, f"{cat}_train_{i}.png"))
        
    start_index = train_amount
    end_index = min(train_amount + test_amount, len(data))
    
    for i in range(start_index, end_index):
        img_array = data[i].reshape(28, 28)
        img_array = 255 - img_array # invert to black lines on white background to match the format of hte test images
        
        img = Image.fromarray(img_array.astype(np.uint8))
        # keep the file name counter starting at 0 for the test batch
        img.save(os.path.join(cat_test_dir, f"{cat}_test_{i - start_index}.png"))
        
    # delete the .npy file to save storage space
    os.remove(file_path) 

print("Successly finished.")