/*
def heads = ["first","second","third"]
def row1 = [1,2,3]
def row2 = [2,2,3]
def row3 = [3,2,3]
def row4 = [4,2,3]
def rows = [row1, row2, row3, row4]

return getAsCSVText(heads, rows)
*/

def getAsCSVText(List heads, List rows) {
    def text = ""
    text += getListAsCSVText(heads)
    rows.each { row ->
        text += getListAsCSVText(row)
    }

    return text
}

def getListAsCSVText(List list) {
    def text = ""
    list.eachWithIndex { elem, index ->
        text += """${elem}${
            (list.size() - 1 > index) ? ";" : """
"""
        }"""
    }
    return text
}
