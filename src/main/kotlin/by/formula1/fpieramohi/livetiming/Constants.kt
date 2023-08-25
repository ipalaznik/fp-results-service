package by.formula1.fpieramohi.livetiming

const val HTTP_NEGOTIATION_URL = "https://livetiming.formula1.com/signalr/negotiate?connectionData=%5B%7B%22name%22%3A%22Streaming%22%7D%5D&clientProtocol=1.5"
const val SIGNALR_PATH =
    "/signalr/connect?clientProtocol=1.5&transport=webSockets&connectionData=%5B%7B%22name%22%3A%22Streaming%22%7D%5D&connectionToken="
const val F1_STREAMING_REQUEST = """
        {
            "H": "Streaming",
            "M": "Subscribe",
            "A": [["TimingData", "Heartbeat", "TimingStats", "TimingAppData", "DriverList"]],
            "I": 1
	    }
        """
