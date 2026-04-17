from flask import Flask, request, jsonify
import tensorflow as tf
import numpy as np

app = Flask(__name__)

# test in console using:
# curl -X POST --data-binary "@drawing_name.png" http://localhost:5000/predict

# load the model once at the top so the server stays speedy
MODEL = tf.keras.models.load_model("drawing_classifier.keras")

CLASS_NAMES = ['apple', 'banana', 'baseball', 'bee', 'bird', 'butterfly', 'camera', 'car', 'cat', 'circle', 'clock', 'cloud', 'cookie', 'dog', 'donut', 'duck', 'envelope', 'eye', 'eyeglasses', 'face', 'fish', 'flower', 'foot', 'fork', 'frog', 'giraffe', 'grapes', 'guitar', 'hand', 'hat', 'horse', 'house', 'ice cream', 'leaf', 'leg', 'light bulb', 'lighthouse', 'lightning', 'lollipop', 'moon', 'mountain', 'octopus', 'palm tree', 'pants', 'peanut', 'scissors', 'shark', 'shoe', 'shovel', 'smiley face', 'snail', 'snowflake', 'snowman', 'square', 'star', 'strawberry', 'sun', 'sword', 'tooth', 'tree', 'triangle', 'wheel', 'windmill']

def get_prediction(input_tensor):
    """Takes a pre-processed tensor and returns the top class, confidence, uncertainty, and all scores."""

    # get the prediction using Sigmoid (independently rates classes rather than rating them as predictions against each other)
    predictions = MODEL.predict(input_tensor)
    scores = tf.nn.sigmoid(predictions[0]).numpy()
    
    # find the highest scoring class
    top_index = np.argmax(scores)
    top_class = CLASS_NAMES[top_index]
    
    # convert numpy float32 to standard Python floats so jsonify doesn't crash
    highest_confidence = float(scores[top_index]) * 100.0
    
    # calculate Uncertainty (how far the top guess is from 100%)
    uncertainty = (1.0 - float(scores[top_index])) * 100.0
    
    # create a dictionary of every class and its individual score
    all_scores = {}
    for i, class_name in enumerate(CLASS_NAMES):
        all_scores[class_name] = float(scores[i]) * 100.0
        
    return top_class, highest_confidence, uncertainty, all_scores

@app.route('/predict', methods=['POST'])
def predict_route():
    try: 
        # catch the byte stream
        raw_bytes = request.get_data()

        # process image in memory (grayscale 28x28)
        matrix = tf.io.decode_image(raw_bytes, channels=1) 
        matrix = tf.image.resize(matrix, [28, 28])
        final_input = tf.expand_dims(matrix, axis=0)

        # get prediction logic
        top_class, confidence, uncertainty, all_scores = get_prediction(final_input)

        # return everything in a JSON payload
        return jsonify({
                "status": "success",
                "top_match": {
                    "class": top_class,
                    "confidence_percent": round(confidence, 2)
                },
                "uncertainty_percent": round(uncertainty, 2),
                "all_scores_percent": {
                    k: round(v, 2) for k, v in all_scores.items()
                }
            }), 200
    
    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
            }), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)