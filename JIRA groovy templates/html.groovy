/*
def heads = ["first","second","third"]
def row1 = [1,2,3]
def row2 = [2,2,3]
def row3 = [3,2,3]
def row4 = [4,2,3]
def rows = [row1, row2, row3, row4]

return getHTMLTable(heads, rows)
*/

def getHTMLTable(List heads, List rows) {
    def html = "<table class='aui'>"
    html += getRowHeadsHTML(heads)
    rows.each { cells ->
        html += getRowHTML(cells)
    }
    html += "</table>"
}

def getRowHTML(List cells) {
    if (cells == null || cells.size() == 0) {
        return ''
    }
    def html = "<tr>"
    cells.each { cell ->
        html += "<td>${cell}</td>"
    }
    html += "</tr>"
}

def getRowHeadsHTML(List heads) {
    if (heads == null || heads.size() == 0) {
        return ''
    }
    def html = "<tr>"
    heads.each { head ->
        html += "<th>${head}</th>"
    }
    html += "</tr>"
}
