import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';            // 👈 map importieren
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

    return this.http.get<any[]>(baseUri, { params }).pipe(
      map(rows => rows.map(o => ({
        id: o.id,
        name: [o.firstName, o.lastName].filter(Boolean).join(' '),  // 👈 hier bauen wir name
        email: o.email
      } as Owner)))
    );
  }

  getById(id: number): Observable<Owner> {
    return this.http.get<any>(`${baseUri}/${id}`).pipe(
      map(o => ({
        id: o.id,
        name: [o.firstName, o.lastName].filter(Boolean).join(' '),  // 👈 ebenso hier
        email: o.email
      } as Owner))
    );
  }
}
