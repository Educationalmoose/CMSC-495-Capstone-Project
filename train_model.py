import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers


data_dir = "Dataset" 

batch_size = 32
img_height = 28
img_width = 28
epochs = 15

print("Num GPUs Available: ", len(tf.config.list_physical_devices('GPU')))

# load the training data set
print("Loading training dataset...")
train_ds = tf.keras.utils.image_dataset_from_directory(
  data_dir,
  validation_split=0.2,
  subset="training",
  seed=123,
  color_mode="grayscale",
  image_size=(img_height, img_width),
  batch_size=batch_size)

# load the validation data set
print("Loading validation dataset...")
val_ds = tf.keras.utils.image_dataset_from_directory(
  data_dir,
  validation_split=0.2,
  subset="validation",
  seed=123,
  color_mode="grayscale",
  image_size=(img_height, img_width),
  batch_size=batch_size)

class_names = train_ds.class_names
print(f"Classes found: {class_names}")

# build the neural network
num_classes = len(class_names)

model = keras.Sequential([
  keras.Input(shape=(img_height, img_width, 1)),
  layers.Rescaling(1./255),
  
  layers.Conv2D(16, 3, padding='same', activation='relu'),
  layers.MaxPooling2D(),
  
  layers.Conv2D(32, 3, padding='same', activation='relu'),
  layers.MaxPooling2D(),
  
  layers.Conv2D(64, 3, padding='same', activation='relu'),
  layers.MaxPooling2D(),
  
  layers.Flatten(),
  layers.Dense(128, activation='relu'),
  layers.Dense(num_classes)
])

# compile the model
model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['accuracy'])

model.summary() # print a table showing the layers of the network

print("Starting training...")
history = model.fit(
  train_ds,
  validation_data=val_ds,
  epochs=epochs
)

print("Training complete. Saving model...")
model.save("drawing_classifier.keras")