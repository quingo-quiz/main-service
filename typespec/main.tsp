import "@typespec/http";
import "./models";
import "./routes";

using Http;

@service(#{ title: "Main Service" })
@server("https://quingo.arhr.tech/api", "Production server")
namespace MainService;
