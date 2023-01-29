import numpy as np
import pandas as pd
from copy import deepcopy


def dissimilarity(arr1, arr2, weighted):
    n = arr1.shape[0]
    s = 0
    if weighted == True:
        for i in range(0, n):
            diff = abs(arr1[i] - arr2[i])
            s = s + (diff * (n - i) / n)
    else:
        for i in range(0, n):
            diff = abs(arr1[i] - arr2[i])
            s = s + (diff)
    return s


def special_sort(a, order="ascending"):
    n = len(a)

    if order == "ascending":
        for i in range(1, n):
            j = deepcopy(i)

            while j > 0 and a[j][1] < a[j - 1][1]:
                temp = a[j - 1]
                a[j - 1] = a[j]
                a[j] = temp

                j = j - 1

    elif order == "descending":
        for i in range(1, n):
            j = deepcopy(i)

            while j > 0 and a[j][1] > a[j - 1][1]:
                temp = a[j - 1]
                a[j - 1] = a[j]
                a[j] = temp

                j = j - 1
    return a


def create_temp_matrix(data):
    userList = data.loc[:, "user"].tolist()
    itemList = data.loc[:, "item"].tolist()
    valueList = data.loc[:, "rating"].tolist()

    users = list(set(data.loc[:, "user"]))
    items = list(set(data.loc[:, "item"]))

    users_index = {users[i]: i for i in range(len(users))}

    pd_dict = {item: [np.nan for _ in range(len(users))] for item in items}

    for i in range(0, len(data)):
        item = itemList[i]
        user = userList[i]
        value = valueList[i]

        pd_dict[item][users_index[user]] = value
        # print i

    X = pd.DataFrame(pd_dict)
    X.index = users

    users = list(X.index)
    items = list(X.columns)

    return (np.array(X), users, items)
