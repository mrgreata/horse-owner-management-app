// src/app/service/owner.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { OwnerCreateDto, OwnerDto } from '../dto/owner';


const baseUri = environment.backendUrl + '/owners';

@Injectable({ providedIn: 'root' })
export class OwnerService {
  constructor(private http: HttpClient) {}

  /** Suche nach Namen (Teilstring); maxAmount begrenzt die Anzahl; beides optional. */
  search(name?: string, maxAmount?: number): Observable<OwnerDto[]> {
    let params = new HttpParams();
    if (name != null && name !== '') { params = params.set('name', name); }
    if (maxAmount != null) { params = params.set('maxAmount', String(maxAmount)); }
    return this.http.get<OwnerDto[]>(baseUri, { params });
  }

  /** Alle holen (einfach die Suche ohne Parameter) */
  getAll(): Observable<OwnerDto[]> {
    return this.http.get<OwnerDto[]>(baseUri);
  }

  /** Owner anlegen – leere Email wird als null gesendet. */
  create(dto: OwnerCreateDto) {
    const payload: OwnerCreateDto = {
      firstName: (dto.firstName ?? '').trim(),
      lastName:  (dto.lastName  ?? '').trim(),
      email: dto.email?.trim() ? dto.email.trim() : null
    };
    return this.http.post<OwnerDto>(baseUri, payload);
  }


  listAll() {
    return this.http.get<OwnerDto[]>(baseUri);
  }

  // Kompatibilitäts-Methode für alten Aufruf
  searchByName(name: string, limit: number) {
    return this.search(name, limit);
  }

}
