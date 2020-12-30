import Foundation
import Capacitor
import Zip

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(ZipManager)
public class ZipManager: CAPPlugin {
    
    /// Called by JS to extract array of zip files to the path specified
    /// - Parameter call: Keys - {files: [String], destination: String}
    @objc func extract(_ call: CAPPluginCall) {
        guard let files = call.getArray("files", String.self),
            let destination = call.getString("destination") else {
                call.reject("Files and destination cannot be empty")
                return
        }
        
        var failedZipList = [String:String]()
        let destinationUrl = URL(fileURLWithPath: destination.replacingOccurrences(of: "file://", with: ""))
        
        for file in files {
            let sourceUrl = URL(fileURLWithPath: file.replacingOccurrences(of: "file://", with: ""))
            do {
                try Zip.unzipFile(sourceUrl, destination: destinationUrl, overwrite: true, password: nil)
            } catch {
                failedZipList[sourceUrl.lastPathComponent] = (error as? ZipError)?.description ?? error.localizedDescription
            }
        }
        
        if failedZipList.count != 0 {
            call.reject("Unable to extract all the files", nil, nil, failedZipList)
            return
        }
        
        call.resolve()
    }
    
    /// Called by JS to compress array of files/ folders to the path specified
    /// - Parameter call: Keys - {files: [String], destination: String}
    @objc func compress(_ call: CAPPluginCall) {
        guard let files = call.getArray("files", String.self),
            let destination = call.getString("destination")
            else {
                call.reject("Files and destination cannot be empty")
                return
        }
        
        let destinationUrl = URL(fileURLWithPath: destination.replacingOccurrences(of: "file://", with: ""))
        let sourceUrls = files.map { URL(fileURLWithPath: $0.replacingOccurrences(of: "file://", with: "")) }
        
        do {
            try Zip.zipFiles(paths: sourceUrls, zipFilePath: destinationUrl, password: nil, progress: nil)
        } catch  {
            try? FileManager.default.removeItem(at: destinationUrl) // If filePath is not valid a zip file will still be created in destination folder. Hence removing that if present.
            call.reject("Unable to compress files - \((error as? ZipError)?.description ?? error.localizedDescription)")
            return
        }
        
        call.resolve()
    }
}
