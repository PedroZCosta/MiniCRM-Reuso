using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers.Api
{
    [Route("api/[controller]")]
    [ApiController]
    public class HistoricoEtapaController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public HistoricoEtapaController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/HistoricoEtapa
        [HttpGet]
        public async Task<ActionResult<IEnumerable<HistoricoEtapa>>> GetHistoricoEtapa()
        {
            return await _context.HistoricoEtapa.ToListAsync();
        }

        // GET: api/HistoricoEtapa/5
        [HttpGet("{id}")]
        public async Task<ActionResult<HistoricoEtapa>> GetHistoricoEtapa(int id)
        {
            var historicoEtapa = await _context.HistoricoEtapa.FindAsync(id);

            if (historicoEtapa == null)
            {
                return NotFound();
            }

            return historicoEtapa;
        }

        // PUT: api/HistoricoEtapa/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutHistoricoEtapa(int id, HistoricoEtapa historicoEtapa)
        {
            if (id != historicoEtapa.IdHistorico)
            {
                return BadRequest();
            }

            _context.Entry(historicoEtapa).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!HistoricoEtapaExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/HistoricoEtapa
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<HistoricoEtapa>> PostHistoricoEtapa(HistoricoEtapa historicoEtapa)
        {
            _context.HistoricoEtapa.Add(historicoEtapa);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetHistoricoEtapa", new { id = historicoEtapa.IdHistorico }, historicoEtapa);
        }

        // DELETE: api/HistoricoEtapa/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteHistoricoEtapa(int id)
        {
            var historicoEtapa = await _context.HistoricoEtapa.FindAsync(id);
            if (historicoEtapa == null)
            {
                return NotFound();
            }

            _context.HistoricoEtapa.Remove(historicoEtapa);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool HistoricoEtapaExists(int id)
        {
            return _context.HistoricoEtapa.Any(e => e.IdHistorico == id);
        }
    }
}
