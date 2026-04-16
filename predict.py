import tensorflow as tf
import numpy as np

# load the trained model
model = tf.keras.models.load_model("drawing_classifier.keras")

# define the parameters
# IF ANYONE CHANGES THIS PLEASE MAKE SURE THE TRAINING PARAMETERS MATCH AND RETRAIN THE MODEL
img_height = 180
img_width = 180

# alphabetical order that the folders of objects are in.
class_names = ["Bird", "Car", "Cat", "Dog", "Face", "Flower", "Fruit", "Gun"] 

# get the location of the drawing
# test_image_path = "Dataset/Cat/0.jpg"
test_image_path = "new_image.png"

# process the image and predict
img = tf.keras.utils.load_img(
    test_image_path, target_size=(img_height, img_width)
)

img_array = tf.keras.utils.img_to_array(img)
img_array = tf.expand_dims(img_array, 0) # create a batch of size 1

predictions = model.predict(img_array)
score = tf.nn.softmax(predictions[0])

print(
    "This image most likely belongs to {} with a {:.2f} percent confidence."
    .format(class_names[np.argmax(score)], 100 * np.max(score))
)