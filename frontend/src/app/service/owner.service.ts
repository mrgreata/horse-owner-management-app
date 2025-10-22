import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Owner } from '../dto/owner';


const baseUri = environment.backendUrl + '/owners';

@Injectable({ providedIn: 'root' })
export class OwnerService {
  constructor(private http: HttpClient) {}

  searchByName(name: string, limit: number): Observable<Owner[]> {
    const params = new HttpParams()
      .set('name', name)
      .set('limit', String(limit));
    return this.http.get<Owner[]>(baseUri, { params });
  }


  getById(id: number): Observable<Owner> {
    return this.http.get<Owner>(`${baseUri}/${id}`);
  }

}
