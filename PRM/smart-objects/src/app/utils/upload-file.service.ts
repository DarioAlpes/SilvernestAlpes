import {Observable} from 'rxjs/Rx';
import {Injectable} from "@angular/core";

@Injectable()
export class UploadFileService {
  progress:any;
  progressObserver:any;
  constructor () {
    this.progress = Observable.create(observer => {
      this.progressObserver = observer
    }).share();
  }

  public makeFileRequest (url: string, file: File): Observable<any> {
    return Observable.create(observer => {
      let formData: FormData = new FormData(),
        xhr: XMLHttpRequest = new XMLHttpRequest();

      formData.append("file", file);

      xhr.onreadystatechange = () => {
        if (xhr.readyState === 4) {
          if (xhr.status === 200) {
            observer.next(JSON.parse(xhr.response));
            observer.complete();
          } else {
            observer.error(xhr.response);
          }
        }
      };

      xhr.upload.onprogress = (event) => {
        this.progress = Math.round(event.loaded / event.total * 100);

        this.progressObserver.next(this.progress);
      };

      xhr.open('POST', url, true);
      xhr.send(formData);
    });
  }
}
