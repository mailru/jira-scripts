methodX = { throwException() }
logging(methodX)

def throwException() {
    throw new Exception('ERROR')
}


def logging(method) {
    try {
        method()
    } catch (any) {
        log.error(e)
    }
}