import os
import tensorflow as tf

data_dir = "Dataset"

removed_count = 0

print("Starting deep clean...")

for dirpath, _, filenames in os.walk(data_dir):
    for filename in filenames:
        file_path = os.path.join(dirpath, filename)
        
        try:
            image_contents = tf.io.read_file(file_path)
            
            _ = tf.image.decode_image(image_contents, channels=3, expand_animations=False)
            
        except Exception as e:
            print(f"Removing unreadable image: {filename}")
            os.remove(file_path)
            removed_count += 1

print(f"Strict clean complete. Removed {removed_count} problematic files.")