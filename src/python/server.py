from flask import Flask, jsonify
from svd import create_svd_matrix, topN_similar
from utils import create_temp_matrix
import pandas as pd

app = Flask(__name__)


@app.route("/topn/<int:user_id>")
def recommend_n_similar(user_id):
    global usk, user_index
    return jsonify(topN_similar(user_id, usk, user_index))


try:
    data = pd.read_csv("./resources/ml-100k/ua.base", delimiter="\t", header=None)
    del data[3]  # Deleting timestamps
    data.columns = ["user", "item", "rating"]
    temp_matrix, users, items = create_temp_matrix(data)
    usk, usv, user_index, prediction_mask = create_svd_matrix(
        temp_matrix, users, items, 15
    )
except:
    print("Couldn't train data")

app.run()
