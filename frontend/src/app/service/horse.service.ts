import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {environment} from 'src/environments/environment';
import {Horse, HorseCreate, HorseSearch} from '../dto/horse';
import {formatIsoDate} from '../utils/date-helper';


const baseUri = environment.backendUrl + '/horses';

@Injectable({ providedIn: 'root' })
export class HorseService {
  constructor(private http: HttpClient) {}

  getAll(): Observable<Horse[]> {
    return this.http.get<Horse[]>(baseUri).pipe(map(hs => hs.map(this.fixHorseDate)));
  }

  /** Suche mit kombinierbaren Parametern (für Autocomplete Mother/Father wichtig) */
  search(params: HorseSearch): Observable<Horse[]> {
    let httpParams = new HttpParams();
    if (params.name) httpParams = httpParams.set('name', params.name);
    if (params.description) httpParams = httpParams.set('description', params.description);
    if (params.sex) httpParams = httpParams.set('sex', params.sex);
    if (params.ownerName) httpParams = httpParams.set('ownerName', params.ownerName);
    if (params.limit != null) httpParams = httpParams.set('limit', String(params.limit));
    if (params.bornBefore) httpParams = httpParams.set('bornBefore', formatIsoDate(params.bornBefore));

    return this.http.get<Horse[]>(baseUri, { params: httpParams })
      .pipe(map(hs => hs.map(this.fixHorseDate)));
  }

  create(horse: HorseCreate): Observable<Horse> {
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    return this.http.post<Horse>(baseUri, horse).pipe(map(this.fixHorseDate));
  }

  getById(id: number): Observable<Horse> {
    return this.http.get<Horse>(`${baseUri}/${id}`).pipe(map(this.fixHorseDate));
  }

  update(id: number, horse: HorseCreate): Observable<Horse> {
    (horse as any).dateOfBirth = formatIsoDate(horse.dateOfBirth);
    return this.http.put<Horse>(`${baseUri}/${id}`, horse).pipe(map(this.fixHorseDate));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${baseUri}/${id}`);
  }

  private fixHorseDate(horse: Horse): Horse {
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    return horse;
  }
}
