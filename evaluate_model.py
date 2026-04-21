import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt
import os

test_dir = "Dataset/Testing"
model_path = "drawing_classifier.keras"
batch_size = 128
img_height = 28
img_width = 28

print("Loading model...")
model = tf.keras.models.load_model(model_path)

print("Loading test dataset...")
test_ds = tf.keras.utils.image_dataset_from_directory(
    test_dir,
    labels='inferred',
    label_mode='int',
    color_mode="grayscale",
    batch_size=batch_size,
    image_size=(img_height, img_width),
    shuffle=False 
)

class_names = test_ds.class_names
num_classes = len(class_names)

print(f"Running predictions on all test images. This will take a moment...")
raw_predictions = model.predict(test_ds)

scores = tf.nn.sigmoid(raw_predictions).numpy() * 100.0 

true_labels = np.concatenate([y for x, y in test_ds], axis=0)

success_rates = []
avg_confidences = []

print("Calculating statistics per category...")
for i, class_name in enumerate(class_names):
    class_indices = np.where(true_labels == i)[0]
    
    class_scores = scores[class_indices]
    
    predicted_classes = np.argmax(class_scores, axis=1)
    
    correct_predictions = np.sum(predicted_classes == i)
    success_rate = (correct_predictions / len(class_indices)) * 100.0
    success_rates.append(success_rate)
    
    true_class_confidences = class_scores[:, i]
    
    avg_confidences.append(np.mean(true_class_confidences))

print("Generating graph...")

plt.figure(figsize=(24, 10))

x_pos = np.arange(num_classes)

plt.bar(x_pos, success_rates, width=0.6, color='skyblue', label='Success Rate (%)', alpha=0.8)


plt.errorbar(
    x_pos, 
    avg_confidences, 
    fmt='o', 
    color='darkorange', 
    ecolor='red', 
    elinewidth=2,
    capsize=4, 
    label='Avg Confidence'
)

plt.xticks(x_pos, class_names, rotation=90, fontsize=10)
plt.yticks(np.arange(0, 101, 10))
plt.ylabel('Percentage (%)', fontsize=12)
plt.xlabel('Categories', fontsize=12)
plt.title('AI Evaluation: Success Rate and Average Confidence', fontsize=16, fontweight='bold')
plt.legend(loc='lower right', fontsize=12)
plt.grid(axis='y', linestyle='--', alpha=0.6)

plt.tight_layout()
plt.savefig('benchmark_results.png', dpi=300)

print("Graph saved successfully as 'benchmark_results.png'!")
plt.show()