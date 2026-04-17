from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np

app = Flask(__name__)

# test in console using:
# curl -X POST --data-binary "drawing_name.png" http://localhost:5000/predict

# load the model once at the top so the server stays speedy
MODEL = tf.keras.models.load_model("drawing_classifier.keras")
CLASS_NAMES = ["Bird", "Car", "Cat", "Dog", "Face", "Flower", "Fruit", "Gun"]

def get_prediction(input_tensor):
    """Takes a pre-processed tensor and returns the prediction string and raw score."""

    # call the prediction method
    predictions = MODEL.predict(input_tensor)
    score = tf.nn.softmax(predictions[0])
    
    class_name = CLASS_NAMES[np.argmax(score)]
    confidence = 100 * np.max(score)
    
    return score, confidence

@app.route('/predict', methods=['POST'])
def predict_route():
    try: 
        # catch the byte stream
        raw_bytes = request.get_data()

        # process image in memory
        matrix = tf.io.decode_image(raw_bytes, channels=3) 
        matrix = tf.image.resize(matrix, [180, 180])
        final_input = tf.expand_dims(matrix, axis=0)

        # get prediction
        message, score = get_prediction(final_input)

        return jsonify({
                "status": "success",
                "class": CLASS_NAMES[np.argmax(score)],
                "confidence": float(np.max(score))
            }), 200
    
    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
            }), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)