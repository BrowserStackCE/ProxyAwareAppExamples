//
//  ViewController.swift
//  ProxyAwareApp
//
//  Created by Rushabh on 13/06/23.
//

import UIKit

class ViewController: UIViewController {
    
    
    @IBOutlet weak var RequestButton: UIButton!
    
    
    @IBOutlet weak var IPText: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
    }
    
    
    @IBAction func OnClickRequestButton(_ sender: Any) {
        let url = "http://ip-api.com/json"
        findProxyFromEnvironment(url: url ,callback: { host,port in
            let config = URLSessionConfiguration.default
            if(host != nil && port != nil){
                config.connectionProxyDictionary = [AnyHashable: Any]()
                config.connectionProxyDictionary?[kCFNetworkProxiesHTTPEnable as String] = 1
                config.connectionProxyDictionary?[kCFNetworkProxiesHTTPProxy as String] = host
                config.connectionProxyDictionary?[kCFNetworkProxiesHTTPPort as String] = port
            }
            let session = URLSession(configuration: config)
            let task = session.dataTask(with: URL(string: url)!)
            { data, response, error in
                DispatchQueue.main.async {
                        self.IPText.text = String(bytes: data!,encoding: String.Encoding.utf8)
                    }
            }
            task.resume()
            })
    }
    
    func findProxyFromEnvironment(url: String,callback: @escaping (_ host:String?,_ port:Int?)->Void) {
        let proxConfigDict = CFNetworkCopySystemProxySettings()?.takeUnretainedValue() as NSDictionary?
        if(proxConfigDict != nil){
            if(proxConfigDict!["ProxyAutoConfigEnable"] as? Int == 1){
                let pacUrl = proxConfigDict!["ProxyAutoConfigURLString"] as? String
                let pacContent = proxConfigDict!["ProxyAutoConfigJavaScript"] as? String
                if(pacContent != nil){
                    self.handlePacContent(pacContent: pacContent! as String, url: url, callback: callback)
                }
                downloadPac(pacUrl: pacUrl!, callback: { pacContent,error in
                    
                    if(error != nil){
                        callback(nil,nil)
                    }else{
                        self.handlePacContent(pacContent: pacContent!, url: url, callback: callback)
                    }
                })
            } else if (proxConfigDict!["HTTPEnable"] as? Int == 1){
                callback((proxConfigDict!["HTTPProxy"] as? String),(proxConfigDict!["HTTPPort"] as? Int))
            } else if ( proxConfigDict!["HTTPSEnable"] as? Int == 1){
                callback((proxConfigDict!["HTTPSProxy"] as? String),(proxConfigDict!["HTTPSPort"] as? Int))
            } else {
                callback(nil,nil)
            }
        }
    }
    
    func handlePacContent(pacContent: String,url: String, callback:(_ host:String?,_ port:Int?)->Void){
        let proxies = CFNetworkCopyProxiesForAutoConfigurationScript(pacContent as CFString, CFURLCreateWithString(kCFAllocatorDefault, url as CFString, nil), nil)!.takeUnretainedValue() as? [[AnyHashable: Any]] ?? [];
        if(proxies.count > 0){
            let proxy = proxies[0]
            if(proxy[kCFProxyTypeKey] as! CFString == kCFProxyTypeHTTP || proxy[kCFProxyTypeKey] as! CFString == kCFProxyTypeHTTPS){
                let host = proxy[kCFProxyHostNameKey]
                let port = proxy[kCFProxyPortNumberKey]
                callback(host as? String,port as? Int)
            }else{
               callback(nil,nil)
            }
        }else{
            callback(nil,nil)
        }
    }
    
    
    func downloadPac(pacUrl:String, callback:@escaping (_ pacContent:String?,_ error: Error?)->Void) {
        var pacContent:String = ""
        let config = URLSessionConfiguration.default
        config.connectionProxyDictionary = [AnyHashable: Any]()
        let session = URLSession.init(configuration: config,delegate: nil,delegateQueue: OperationQueue.current)
        session.dataTask(with: URL(string: pacUrl)!, completionHandler: { data, response, error in
            if(error != nil){
                callback(nil,error)
            }
            pacContent = String(bytes: data!,encoding: String.Encoding.utf8)!
            callback(pacContent,nil)
        }).resume()

    }
}
